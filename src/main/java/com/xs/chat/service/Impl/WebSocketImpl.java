package com.xs.chat.service.Impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xs.chat.Configure.ThreadPoolConfig;
import com.xs.chat.Utils.NettyUtil;
import com.xs.chat.context.BaseContext;
import com.xs.chat.event.MessageSentEvent;
import com.xs.chat.event.UserOfflineEvent;
import com.xs.chat.event.UserOnlineEvent;
import com.xs.chat.mapper.ChatRoomMapper;
import com.xs.chat.mapper.ChatRoomMemberMapper;
import com.xs.chat.mapper.MessageMapper;
import com.xs.chat.mapper.UserInfoMapper;
import com.xs.chat.pojo.DO.ChatRoomDO;
import com.xs.chat.pojo.DO.ChatRoomMemberDO;
import com.xs.chat.pojo.DO.MessageDO;
import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.pojo.DTO.MessageDTO;
import com.xs.chat.pojo.VO.ChatRoomVO;
import com.xs.chat.pojo.VO.UserVO;
import com.xs.chat.service.AuthService;
import com.xs.chat.service.ChatRoomService;
import com.xs.chat.service.WebSocketService;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WebSocketImpl implements WebSocketService {

    private static final ConcurrentHashMap<String, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Set<Long>> USER_GROUP_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, ChannelGroup> GROUP_CHANNEL_MAP = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private ChatRoomMapper chatRoomMapper;
    @Autowired
    private ChatRoomMemberMapper chatRoomMemberMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    @Qualifier(ThreadPoolConfig.WS_EXECUTOR)
    private ThreadPoolTaskExecutor executor;
    @Override
    public void onConnect(Channel channel) {
        log.info("新连接 - channelId: {}", channel.id());
    }

    @Override
    public void disconnect(Channel channel) {
        String userId = NettyUtil.getAttr(channel, NettyUtil.ID);
        if (userId != null) {
            // 检查是否已经处理过离线
            Channel existingChannel = USER_CHANNEL_MAP.get(userId);
            if (existingChannel != channel) {
                // 说明当前 channel 已经不是活跃的连接，跳过处理
                log.debug("跳过重复的断开连接 - userId: {}", userId);
                channel.close();
                return;
            }
            USER_CHANNEL_MAP.remove(userId);
            Set<Long> groupIds = USER_GROUP_MAP.remove(userId);
            if (groupIds != null) {
                for (Long groupId : groupIds) {
                    ChannelGroup group = GROUP_CHANNEL_MAP.get(groupId);
                    if (group != null) {
                        group.remove(channel);
                        if (group.isEmpty()) {
                            GROUP_CHANNEL_MAP.remove(groupId);
                        }
                    }
                }
            }
            UserDO user = new UserDO();
            user.setId(userId);
            user.setLastLoginAt(LocalDate.now());
            applicationEventPublisher.publishEvent(new UserOfflineEvent(this, user));
            log.info("用户离线 - userId: {}", userId);
        }
        channel.close();
    }

    @Override
    public String authenticate(Channel channel, String token) {
        String userId = authService.validateToken(token);
        if (userId == null) {
            return null;
        }
        Channel oldChannel = USER_CHANNEL_MAP.put(userId, channel);
        if (oldChannel != null && oldChannel.isActive() && oldChannel != channel) {
            oldChannel.close();
        }
        NettyUtil.setAttr(channel, NettyUtil.ID, userId);
        autoSubscribeGroups(userId, channel);
        handleOnline(channel);
        UserDO user = new UserDO();
        user.setId(userId);
        user.setLastLoginAt(LocalDate.now());
        applicationEventPublisher.publishEvent(new UserOnlineEvent(this, user));
        return userId;
    }

    private void handleOnline(Channel channel) {
        List<String> onlineUsers = USER_CHANNEL_MAP.keySet().stream().toList();
        List<UserVO> userVOs = userInfoMapper.selectBatchIds(onlineUsers).stream().map(
                userDO -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(userDO, userVO);
                    userVO.setOnlineStatus(1);
                    return userVO;
                }
        ).collect(Collectors.toList());
        sendMsg(channel, "{\"type\":\"ONLINE_LIST\",\"users\":" + JSONUtil.toJsonStr(userVOs) + "}");
    }

    @Override
    public void handleMessage(Channel channel, String content) {
        String userId = NettyUtil.getAttr(channel, NettyUtil.ID);
        if (userId == null) {
            log.warn("未认证用户尝试发送消息");
            return;
        }
        try {
            BaseContext.setCurrentId(userId);
            MessageDTO messageDTO = JSONUtil.toBean(content, MessageDTO.class);
            String type = messageDTO.getType();
            if ("JOIN_GROUP".equals(type)) {
                joinGroup(userId, channel, messageDTO.getChatRoomId(), true);
            } else if ("LEAVE_GROUP".equals(type)) {
                handleLeaveGroup(userId, channel, messageDTO);
            } else if ("GROUP".equals(type)) {
                handleGroupMessage(userId, messageDTO);

            } else {
                log.warn("未知消息类型: {}", type);
            }
        }catch (Exception e){
            log.warn("处理消息异常", e);
        }
        finally {
            BaseContext.removeCurrentId();
        }
    }

    private void autoSubscribeGroups(String userId, Channel channel) {
        LambdaQueryWrapper<ChatRoomMemberDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatRoomMemberDO::getUserId, userId);
        List<Long> roomIdList = chatRoomMemberMapper.selectList(queryWrapper).stream().map(ChatRoomMemberDO::getChatRoomId).collect(Collectors.toList());
        LambdaQueryWrapper<ChatRoomDO> roomQueryWrapper = new LambdaQueryWrapper<>();
        roomQueryWrapper.in(ChatRoomDO::getId, roomIdList);
        chatRoomMapper.selectList(roomQueryWrapper).forEach(room -> joinGroup(userId, channel, room.getId(), true));
    }


    private void joinGroup(String userId, Channel channel, Long roomId, boolean ack) {
        if (roomId == null) {
            if (ack) {
                sendMsg(channel, "{\"type\":\"ERROR\",\"content\":\"chatRoomId不能为空\"}");
            }
            return;
        }
        ChannelGroup group = GROUP_CHANNEL_MAP.computeIfAbsent(roomId, k -> new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
        group.add(channel);
        USER_GROUP_MAP.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(roomId);
        if (ack) {
            sendMsg(channel, "{\"type\":\"JOIN_GROUP_ACK\",\"chatRoomId\":" + roomId + "}");
        }
    }

    private void handleLeaveGroup(String userId, Channel channel, MessageDTO messageDTO) {
        Long roomId = messageDTO.getChatRoomId();
        if (roomId == null) {
            return;
        }
        ChannelGroup group = GROUP_CHANNEL_MAP.get(roomId);
        if (group != null) {
            group.remove(channel);
            if (group.isEmpty()) {
                GROUP_CHANNEL_MAP.remove(roomId);
            }
        }
        Set<Long> groupIds = USER_GROUP_MAP.get(userId);
        if (groupIds != null) {
            groupIds.remove(roomId);
        }
    }

    private void handleGroupMessage(String userId, MessageDTO messageDTO) {
        Long roomId = messageDTO.getChatRoomId();
        if (roomId == null) {
            return;
        }
        Set<Long> joined = USER_GROUP_MAP.get(userId);
        if (joined == null || !joined.contains(roomId)) {
            Channel channel = USER_CHANNEL_MAP.get(userId);
            if (channel != null) {
                sendMsg(channel, "{\"type\":\"ERROR\",\"content\":\"请先加入群组\"}");
            }
            return;
        }
        //检查禁言
        ChatRoomMemberDO member = chatRoomMemberMapper.selectByChatRoomIdAndUserId(roomId,userId);
        if (member != null && member.getIsMuted() != null && member.getIsMuted() == 1) {
            Channel channel = USER_CHANNEL_MAP.get(userId);
            if (channel != null) {
                sendMsg(channel, "{\"type\":\"ERROR\",\"content\":\"你已被禁言，无法发送消息\"}");
            }
            return;
        }
        applicationEventPublisher.publishEvent(new MessageSentEvent(this, messageDTO));
        ChannelGroup group = GROUP_CHANNEL_MAP.get(roomId);
        if (group == null) {
            return;
        }
        messageDTO.setSenderId(userId);
        LambdaQueryWrapper<MessageDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MessageDO::getChatRoomId, roomId);
        queryWrapper.orderByDesc(MessageDO::getId);
        queryWrapper.last("limit 1");
        messageDTO.setMessageId(messageMapper.selectOne(queryWrapper).getId());
        log.info("发送群组消息: {}", JSONUtil.toJsonStr(messageDTO));
        group.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(messageDTO)));

    }
    //通知上线
    @Override
    public void updateOnlineBroadcast() {
        USER_CHANNEL_MAP.forEach(((s, channel) ->  {
            executor.execute(() -> {
                handleOnline(channel);
            });
        }));
    }

    //通知群聊创建
    @Override
    public void groupCreatedBroadcast(ChatRoomVO chatRoomVO) {
        LambdaQueryWrapper<ChatRoomMemberDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatRoomMemberDO::getChatRoomId, chatRoomVO.getId());
        List<String> idList = chatRoomMemberMapper.selectList(queryWrapper).stream().map(ChatRoomMemberDO::getUserId).collect(Collectors.toList());
        USER_CHANNEL_MAP.forEach(((s, channel) -> {
            if (idList.contains(s)) {
                executor.execute(() -> {
                    sendMsg(channel, "{\"type\":\"GROUP_CREATED\",\"chatRoom\":" + JSONUtil.toJsonStr(chatRoomVO) + "}");
                });
            }
        }));
    }

    private void sendMsg(Channel channel, String msg) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(new TextWebSocketFrame(msg));
            log.info("发送消息: {}", msg);
        }
    }
}