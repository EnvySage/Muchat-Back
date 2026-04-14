package com.xs.chat.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import cn.hutool.json.JSONUtil;
import com.xs.chat.Exception.BusinessException;
import com.xs.chat.context.BaseContext;
import com.xs.chat.enumeration.ChatRoomEnum;
import com.xs.chat.enumeration.NoticeStatusEnum;
import com.xs.chat.enumeration.NoticeTypeEnum;
import com.xs.chat.enumeration.ResponseCodeEnum;
import com.xs.chat.enumeration.permission.GroupRoleEnum;
import com.xs.chat.event.GroupCreatedEvent;
import com.xs.chat.event.GroupPermissionUpdateEvent;
import com.xs.chat.mapper.ChatRoomMapper;
import com.xs.chat.mapper.ChatRoomMemberMapper;
import com.xs.chat.mapper.MessageMapper;
import com.xs.chat.mapper.UserInfoMapper;
import com.xs.chat.pojo.DO.ChatRoomDO;
import com.xs.chat.pojo.DO.ChatRoomMemberDO;
import com.xs.chat.pojo.DO.MessageDO;
import com.xs.chat.pojo.DO.SystemNoticeDO;
import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.pojo.DTO.ChatRoomDTO;
import com.xs.chat.pojo.DTO.ChatRoomMemberDTO;
import com.xs.chat.pojo.Result;
import com.xs.chat.pojo.VO.ChatRoomMemberVO;
import com.xs.chat.pojo.VO.ChatRoomVO;
import com.xs.chat.service.ChatRoomService;
import com.xs.chat.service.NoticeService;
import com.xs.chat.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@Service
public class ChatRoomImpl implements ChatRoomService {
    @Autowired
    private ChatRoomMapper chatRoomMapper;
    @Autowired
    private ChatRoomMemberMapper chatRoomMemberMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private NoticeService noticeService;
    @Override
    public void joinRoom(ChatRoomMemberDTO chatRoomMemberDTO) {
        ChatRoomMemberDO chatRoomMemberDO = new ChatRoomMemberDO();
        BeanUtils.copyProperties(chatRoomMemberDTO,chatRoomMemberDO);
        chatRoomMemberDO.setUserId(BaseContext.getCurrentId());
        chatRoomMemberMapper.insert(chatRoomMemberDO);
        log.info("joinRoom:{}",chatRoomMemberDO);
    }

    @Override
    public List<ChatRoomVO> getAllRoom() {
        String currentUserId = BaseContext.getCurrentId();

        // 查询自己创建的聊天室
        LambdaQueryWrapper<ChatRoomDO> creatorQueryWrapper = new LambdaQueryWrapper<>();
        creatorQueryWrapper.eq(ChatRoomDO::getCreatorId, currentUserId);
        List<ChatRoomDO> createdRooms = chatRoomMapper.selectList(creatorQueryWrapper);

        // 查询自己参加的聊天室
        LambdaQueryWrapper<ChatRoomMemberDO> memberQueryWrapper = new LambdaQueryWrapper<>();
        memberQueryWrapper.eq(ChatRoomMemberDO::getUserId, currentUserId);
        List<Long> joinedRoomIds = chatRoomMemberMapper.selectList(memberQueryWrapper)
                .stream()
                .map(ChatRoomMemberDO::getChatRoomId)
                .collect(Collectors.toList());

        List<ChatRoomDO> joinedRooms = new ArrayList<>();
        if (!joinedRoomIds.isEmpty()) {
            LambdaQueryWrapper<ChatRoomDO> joinedQueryWrapper = new LambdaQueryWrapper<>();
            joinedQueryWrapper.in(ChatRoomDO::getId, joinedRoomIds);
            joinedRooms = chatRoomMapper.selectList(joinedQueryWrapper);
        }

        // 合并并去重
        List<ChatRoomDO> allRooms = new ArrayList<>();
        allRooms.addAll(createdRooms);
        joinedRooms.stream()
                .filter(room -> createdRooms.stream()
                        .noneMatch(created -> created.getId().equals(room.getId())))
                .forEach(allRooms::add);



        return allRooms.stream().map(item -> {
            ChatRoomVO chatRoomVO = new ChatRoomVO();
            BeanUtils.copyProperties(item, chatRoomVO);
            
            // 查询最后一条消息
            MessageDO latestMessage = messageMapper.selectLatestMessage(item.getId());
            if (latestMessage != null) {
                chatRoomVO.setLastMessageContent(latestMessage.getContent());
                chatRoomVO.setLastMessageSenderId(latestMessage.getSenderId());
                chatRoomVO.setLastMessageSentAt(latestMessage.getSentAt());
                // 查询发送者名字
                UserDO sender = userInfoMapper.selectById(latestMessage.getSenderId());
                if (sender != null) {
                    chatRoomVO.setLastMessageSenderName(sender.getNickname());
                }
            }

            LambdaQueryWrapper<ChatRoomMemberDO> memberQuery = new LambdaQueryWrapper<>();
            memberQuery.eq(ChatRoomMemberDO::getChatRoomId, item.getId())
                    .eq(ChatRoomMemberDO::getUserId, currentUserId);
            ChatRoomMemberDO member = chatRoomMemberMapper.selectOne(memberQuery);

            Long lastReadMessageId = (member != null && member.getLastReadMessageId() != null)
                    ? member.getLastReadMessageId() : 0L;
            Long unreadCount = Long.valueOf(messageMapper.countUnreadMessages(item.getId(), lastReadMessageId));
            chatRoomVO.setUnreadCount(unreadCount);
            //封装成员
            List<ChatRoomMemberVO> memberVOS = getChatRoomMembers(item.getId());
            chatRoomVO.setMemberCount(memberVOS.size());
            chatRoomVO.setMembers(memberVOS);
            return chatRoomVO;
        }).collect(Collectors.toList());

    }

    @Override
    public ChatRoomVO createRoom(ChatRoomDTO chatRoomDTO) {
        ChatRoomDO chatRoomDO = new ChatRoomDO();
        BeanUtils.copyProperties(chatRoomDTO, chatRoomDO);
        chatRoomDO.setCreatorId(BaseContext.getCurrentId());
        chatRoomDO.setCreatedAt(LocalDateTime.now());
        int row = chatRoomMapper.insert(chatRoomDO);

        // 创建者自身加入群聊（群主）
        ChatRoomMemberDO ownerMember = new ChatRoomMemberDO();
        ownerMember.setChatRoomId(chatRoomDO.getId());
        ownerMember.setUserId(BaseContext.getCurrentId());
        ownerMember.setRole(GroupRoleEnum.OWNER.getCode());
        chatRoomMemberMapper.BatchInsert(List.of(ownerMember));

        if (chatRoomDTO.getMemberIdList() != null && !chatRoomDTO.getMemberIdList().isEmpty()) {
            String inviterId = BaseContext.getCurrentId();
            UserDO inviter = userInfoMapper.selectById(inviterId);
            String inviterName = inviter != null ? inviter.getNickname() : "未知用户";

            for (String userId : chatRoomDTO.getMemberIdList()) {
                // 创建群邀请通知，被邀请人需同意后才加入
                SystemNoticeDO notice = new SystemNoticeDO();
                notice.setReceiverId(userId);
                notice.setSenderId(inviterId);
                notice.setType(NoticeTypeEnum.GROUP_INVITE.getCode());
                notice.setTitle(inviterName + " 邀请你加入群聊 " + chatRoomDO.getName());
                notice.setRelatedId(String.valueOf(chatRoomDO.getId()));
                notice.setExtraData(JSONUtil.toJsonStr(Map.of(
                        "chatRoomName", chatRoomDO.getName() != null ? chatRoomDO.getName() : "",
                        "chatRoomAvatar", chatRoomDO.getAvatarUrl() != null ? chatRoomDO.getAvatarUrl() : "",
                        "inviterName", inviterName
                )));
                notice.setStatus(NoticeStatusEnum.UNREAD.getCode());
                notice.setExpiredAt(LocalDateTime.now().plusDays(7));
                noticeService.sendNotice(notice);
            }
        }

        if (row > 0) {
            ChatRoomVO chatRoomVO = new ChatRoomVO();
            BeanUtils.copyProperties(chatRoomDO, chatRoomVO);
            // 填充成员信息（创建者自己）
            List<ChatRoomMemberVO> memberVOS = getChatRoomMembers(chatRoomDO.getId());
            chatRoomVO.setMembers(memberVOS);
            chatRoomVO.setMemberCount(memberVOS.size());
            chatRoomVO.setIsActive(1);
            chatRoomVO.setIsPin(0);
            applicationEventPublisher.publishEvent(new GroupCreatedEvent(this, chatRoomVO, chatRoomDTO.getMemberIdList()));
            log.info("createRoom:{}", chatRoomVO);
            return chatRoomVO;
        } else {
            log.error("createRoom error:{}", chatRoomDTO);
            return null;
        }
    }

    // 批量插入聊天室成员
    private void batchInsertChatRoomMembers(Long chatRoomId, List<String> memberIdList) {
        // 创建普通成员列表
        List<ChatRoomMemberDO> chatRoomMemberDOList = memberIdList.stream().map(userId -> {
            ChatRoomMemberDO chatRoomMemberDO = new ChatRoomMemberDO();
            chatRoomMemberDO.setChatRoomId(chatRoomId);
            chatRoomMemberDO.setUserId(userId);
            chatRoomMemberDO.setRole(GroupRoleEnum.MEMBER.getCode());
            return chatRoomMemberDO;
        }).collect(Collectors.toList());
        // 批量插入
        chatRoomMemberMapper.BatchInsert(chatRoomMemberDOList);
        log.info("批量插入聊天室成员成功，房间ID: {}, 成员数量: {}", chatRoomId, chatRoomMemberDOList.size());
    }

    @Override
    public ChatRoomMemberVO unreadMessage(ChatRoomMemberDTO chatRoomMemberDTO) {
        String currentUserId = BaseContext.getCurrentId();
        Long chatRoomId = chatRoomMemberDTO.getChatRoomId();

        // 1. 获取用户在聊天室的当前 lastReadMessageId
        if (chatRoomMemberDTO.getLastReadMessageId() == null){
            return null;
        }
        ChatRoomMemberDO currentMember = chatRoomMemberMapper.selectByChatRoomIdAndUserId(chatRoomId, currentUserId);

        // 如果成员不存在，返回空结果
        if (currentMember == null) {
            log.warn("用户 {} 不在聊天室 {} 中", currentUserId, chatRoomId);
            return new ChatRoomMemberVO();
        }

        Long lastReadMessageId = currentMember.getLastReadMessageId();
        if (lastReadMessageId == null) {
            lastReadMessageId = 0L;
        }

        // 2. 统计未读消息数量
        Integer unreadCount = messageMapper.countUnreadMessages(chatRoomId, lastReadMessageId);

        // 3. 获取最新一条消息
        MessageDO latestMessage = messageMapper.selectLatestMessage(chatRoomId);
        Long latestMessageId = (latestMessage != null) ? latestMessage.getId() : lastReadMessageId;

        // 4. 更新 lastReadMessageId 为最新消息ID
        chatRoomMemberMapper.updateLastReadMessageId(chatRoomId, currentUserId, latestMessageId);

        // 5. 构造返回结果
        ChatRoomMemberVO result = new ChatRoomMemberVO();
        result.setId(currentMember.getId());
        result.setChatRoomId(chatRoomId);
        result.setUserId(currentUserId);
        result.setRole(currentMember.getRole());
        result.setJoinedAt(currentMember.getJoinedAt());
        result.setLastReadMessageId(latestMessageId);
        result.setIsMuted(currentMember.getIsMuted());
        result.setIsVisible(currentMember.getIsVisible());
        result.setUnreadCount(unreadCount);

        log.info("更新未读消息，userId: {}, chatRoomId: {}, 未读数: {}", currentUserId, chatRoomId, unreadCount);
        return result;
    }

    @Override
    public void updateGroupNickname(ChatRoomMemberDTO chatRoomMemberDTO) {
        ChatRoomMemberDO exitMember = chatRoomMemberMapper.selectByChatRoomIdAndUserId(chatRoomMemberDTO.getChatRoomId(), chatRoomMemberDTO.getUserId());
        if (exitMember == null){
            log.error("updateGroupNickname error:{}",chatRoomMemberDTO);
            return;
        }
        exitMember.setRoomName(chatRoomMemberDTO.getRoomName());
        chatRoomMemberMapper.updateById(exitMember);
    }

    @Override
    public void inviteGroup(ChatRoomMemberDTO chatRoomMemberDTO) {
        Long chatRoomId = chatRoomMemberDTO.getChatRoomId();
        ChatRoomDO room = chatRoomMapper.selectById(chatRoomId);
        if (room == null || room.getIsActive() == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "群聊不存在或已解散");
        }
        String inviterId = BaseContext.getCurrentId();
        UserDO inviter = userInfoMapper.selectById(inviterId);
        String inviterName = inviter != null ? inviter.getNickname() : "未知用户";

        for (String userId : chatRoomMemberDTO.getUserIdList()) {
            // 跳过已在群中的用户
            ChatRoomMemberDO existing = chatRoomMemberMapper.selectByChatRoomIdAndUserId(chatRoomId, userId);
            if (existing != null) {
                log.info("inviteGroup: 用户{}已在群中，跳过", userId);
                continue;
            }

            // 创建群邀请通知
            SystemNoticeDO notice = new SystemNoticeDO();
            notice.setReceiverId(userId);
            notice.setSenderId(inviterId);
            notice.setType(NoticeTypeEnum.GROUP_INVITE.getCode());
            notice.setTitle(inviterName + " 邀请你加入群聊 " + room.getName());
            notice.setRelatedId(String.valueOf(chatRoomId));
            notice.setExtraData(JSONUtil.toJsonStr(Map.of(
                    "chatRoomName", room.getName() != null ? room.getName() : "",
                    "chatRoomAvatar", room.getAvatarUrl() != null ? room.getAvatarUrl() : "",
                    "inviterName", inviterName
            )));
            notice.setStatus(NoticeStatusEnum.UNREAD.getCode());
            notice.setExpiredAt(LocalDateTime.now().plusDays(7));
            noticeService.sendNotice(notice);
        }
        log.info("inviteGroup: 已发送群邀请通知, chatRoomId={}, userIdList={}", chatRoomId, chatRoomMemberDTO.getUserIdList());
    }

    @Override
    public boolean kickGroup(ChatRoomMemberDTO chatRoomMemberDTO) {
        String currentUserId = BaseContext.getCurrentId();
        Long chatRoomId = chatRoomMemberDTO.getChatRoomId();
        List<String> targetUserIds = chatRoomMemberDTO.getUserIdList();

        if (targetUserIds == null || targetUserIds.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "请选择要踢出的成员");
        }

        // 不能踢自己
        if (targetUserIds.contains(currentUserId)) {
            log.warn("kickGroup: 不能踢自己, userId={}", currentUserId);
            throw new BusinessException(ResponseCodeEnum.CODE_600, "不能踢自己出群");
        }

        GroupRoleEnum currentRole = permissionService.getRole(chatRoomId, currentUserId);

        // 逐个校验被踢人的角色
        for (String targetUserId : targetUserIds) {
            ChatRoomMemberDO targetMember = chatRoomMemberMapper.selectByChatRoomIdAndUserId(chatRoomId, targetUserId);
            if (targetMember == null) {
                log.warn("kickGroup: 被踢用户不在群聊中, targetUserId={}", targetUserId);
                throw new BusinessException(ResponseCodeEnum.CODE_600, "用户 " + targetUserId + " 不在群聊中");
            }

            GroupRoleEnum targetRole = GroupRoleEnum.fromCode(targetMember.getRole());
            // 群主不可被踢
            if (GroupRoleEnum.OWNER.equals(targetRole)) {
                log.warn("kickGroup: 不能踢出群主, targetUserId={}", targetUserId);
                throw new BusinessException(ResponseCodeEnum.CODE_403, "不能踢出群主");
            }

            // 管理员不能踢管理员（只有群主可以踢管理员）
            if (GroupRoleEnum.ADMIN.equals(currentRole) && GroupRoleEnum.ADMIN.equals(targetRole)) {
                log.warn("kickGroup: 管理员不能踢出其他管理员, currentUserId={}, targetUserId={}", currentUserId, targetUserId);
                throw new BusinessException(ResponseCodeEnum.CODE_403, "管理员不能踢出其他管理员");
            }
        }

        // 批量删除成员
        LambdaQueryWrapper<ChatRoomMemberDO> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ChatRoomMemberDO::getChatRoomId, chatRoomId)
                .in(ChatRoomMemberDO::getUserId, targetUserIds);
        int rows = chatRoomMemberMapper.delete(deleteWrapper);
        if (rows > 0) {
            // 批量清除角色缓存
            for (String targetUserId : targetUserIds) {
                permissionService.cleanRoleCache(chatRoomId, targetUserId);
            }
            // 广播踢人通知
            applicationEventPublisher.publishEvent(new GroupPermissionUpdateEvent(this, chatRoomId, "KICK",
                    "{\"userIds\":" + JSONUtil.toJsonStr(targetUserIds) + "}"));
            log.info("kickGroup: 批量踢出成功, chatRoomId={}, targetUserIds={}, count={}", chatRoomId, targetUserIds, rows);
            return true;
        }
        return false;
    }

    @Override
    public boolean exitRoom(ChatRoomMemberDTO chatRoomMemberDTO) {
        ChatRoomMemberDO chatRoomMemberDO = new ChatRoomMemberDO();
        chatRoomMemberDO.setChatRoomId(chatRoomMemberDTO.getChatRoomId());
        chatRoomMemberDO.setUserId(BaseContext.getCurrentId());
        int row = chatRoomMemberMapper.delete(new LambdaQueryWrapper<>(chatRoomMemberDO));
        if (row > 0) {
            permissionService.cleanRoleCache(chatRoomMemberDTO.getChatRoomId(), BaseContext.getCurrentId());
            return true;
        }
        return false;
    }

    @Override
    public boolean updateRoom(ChatRoomDTO chatRoomDTO) {
        ChatRoomDO chatRoomDO = new ChatRoomDO();
        BeanUtils.copyProperties(chatRoomDTO, chatRoomDO);
        chatRoomDO.setId(chatRoomDTO.getChatRoomId());
        int row = chatRoomMapper.updateById(chatRoomDO);
        if (row > 0) {
            // 广播群信息更新通知
            applicationEventPublisher.publishEvent(new GroupPermissionUpdateEvent(this, chatRoomDTO.getChatRoomId(), "GROUP_INFO_UPDATE",
                    JSONUtil.toJsonStr(chatRoomDTO)));
            return true;
        }
        return false;
    }

    @Override
    public boolean batchMute(ChatRoomMemberDTO chatRoomMemberDTO) {
        String currentUserId = BaseContext.getCurrentId();
        Long chatRoomId = chatRoomMemberDTO.getChatRoomId();
        List<String> targetUserIds = chatRoomMemberDTO.getUserIdList();
        Integer isMuted = chatRoomMemberDTO.getIsMuted();

        if (targetUserIds == null || targetUserIds.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "请选择要操作的成员");
        }
        if (isMuted == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "请指定禁言或解禁操作");
        }

        // 不能禁言自己
        if (targetUserIds.contains(currentUserId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "不能对自己进行禁言操作");
        }

        GroupRoleEnum currentRole = permissionService.getRole(chatRoomId, currentUserId);

        // 逐个校验目标成员角色
        for (String targetUserId : targetUserIds) {
            ChatRoomMemberDO targetMember = chatRoomMemberMapper.selectByChatRoomIdAndUserId(chatRoomId, targetUserId);
            if (targetMember == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_600, "用户 " + targetUserId + " 不在群聊中");
            }

            GroupRoleEnum targetRole = GroupRoleEnum.fromCode(targetMember.getRole());
            // 不能禁言群主
            if (GroupRoleEnum.OWNER.equals(targetRole)) {
                throw new BusinessException(ResponseCodeEnum.CODE_403, "不能禁言群主");
            }
            // 管理员不能禁言其他管理员
            if (GroupRoleEnum.ADMIN.equals(currentRole) && GroupRoleEnum.ADMIN.equals(targetRole)) {
                throw new BusinessException(ResponseCodeEnum.CODE_403, "管理员不能禁言其他管理员");
            }
        }

        // 批量更新禁言状态
        LambdaQueryWrapper<ChatRoomMemberDO> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(ChatRoomMemberDO::getChatRoomId, chatRoomId)
                .in(ChatRoomMemberDO::getUserId, targetUserIds);

        ChatRoomMemberDO updateDO = new ChatRoomMemberDO();
        updateDO.setIsMuted(isMuted);
        int rows = chatRoomMemberMapper.update(updateDO, updateWrapper);

        if (rows > 0) {
            // 广播禁言/解禁通知
            applicationEventPublisher.publishEvent(new GroupPermissionUpdateEvent(this, chatRoomId, isMuted == 1 ? "MUTE" : "UNMUTE",
                    "{\"userIds\":" + JSONUtil.toJsonStr(targetUserIds) + ",\"isMuted\":" + isMuted + "}"));
            log.info("batchMute: 批量{}成功, chatRoomId={}, targetUserIds={}, count={}",
                    isMuted == 1 ? "禁言" : "解禁", chatRoomId, targetUserIds, rows);
            return true;
        }
        return false;
    }

    @Override
    public boolean dismissRoom(Long chatRoomId) {
        ChatRoomDO chatRoomDO = chatRoomMapper.selectById(chatRoomId);
        if (chatRoomDO == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "聊天室不存在");
        }
        chatRoomDO.setIsActive(0);
        int row = chatRoomMapper.updateById(chatRoomDO);
        if (row > 0) {
            // 将待处理的群邀请通知设为过期
            noticeService.expireGroupInvites(chatRoomId);
            // 广播群聊解散通知
            applicationEventPublisher.publishEvent(new GroupPermissionUpdateEvent(this, chatRoomId, "DISMISS", "{}"));
            return true;
        }
        return false;
    }

    @Override
    public boolean changeAdmin(ChatRoomMemberDTO chatRoomMemberDTO) {
        Integer isAdmin = chatRoomMemberDTO.getIsAdmin();
        if (isAdmin == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "请指定设置或取消管理员操作");
        }
        LambdaUpdateWrapper<ChatRoomMemberDO> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.eq(ChatRoomMemberDO::getChatRoomId, chatRoomMemberDTO.getChatRoomId())
                .eq(ChatRoomMemberDO::getUserId, chatRoomMemberDTO.getUserId());
        ChatRoomMemberDO chatRoomMemberDO = chatRoomMemberMapper.selectOne(queryWrapper);
        if (chatRoomMemberDO == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "用户不在群聊中");
        }
        queryWrapper.set(ChatRoomMemberDO::getRole, isAdmin == 1 ? GroupRoleEnum.ADMIN.getCode() : GroupRoleEnum.MEMBER.getCode());
        int row = chatRoomMemberMapper.update(null, queryWrapper);
        if (row > 0) {
            permissionService.cleanRoleCache(chatRoomMemberDTO.getChatRoomId(), chatRoomMemberDTO.getUserId());
            // 广播管理员变更通知
            applicationEventPublisher.publishEvent(new GroupPermissionUpdateEvent(this, chatRoomMemberDTO.getChatRoomId(),
                    isAdmin == 1 ? "ADMIN_ADD" : "ADMIN_REMOVE",
                    "{\"userId\":\"" + chatRoomMemberDTO.getUserId() + "\",\"isAdmin\":" + isAdmin + "}"));
            log.info("changeAdmin: {}成功, chatRoomId={}, userId={}",
                    isAdmin == 1 ? "设置管理员" : "取消管理员", chatRoomMemberDTO.getChatRoomId(), chatRoomMemberDTO.getUserId());
            return true;
        }
        return false;
    }

    @Override
    public List<ChatRoomVO> searchPublicRooms(String keyword) {
        LambdaQueryWrapper<ChatRoomDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatRoomDO::getType, ChatRoomEnum.PUBLIC.getCode())
                .like(ChatRoomDO::getName, keyword)
                .eq(ChatRoomDO::getIsActive, 1)
                .last("LIMIT 20");
        List<ChatRoomDO> chatRoomDOList = chatRoomMapper.selectList(queryWrapper);
        return chatRoomDOList.stream().map(item -> {
            ChatRoomVO chatRoomVO = new ChatRoomVO();
            BeanUtils.copyProperties(item, chatRoomVO);
            List<ChatRoomMemberVO> memberVOS = getChatRoomMembers(item.getId());
            chatRoomVO.setMemberCount(memberVOS.size());
            return chatRoomVO;
        }).collect(Collectors.toList());
    }

    // 获取聊天室成员列表
    private List<ChatRoomMemberVO> getChatRoomMembers(Long chatRoomId) {
        // 查询聊天室所有成员
        LambdaQueryWrapper<ChatRoomMemberDO> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(ChatRoomMemberDO::getChatRoomId, chatRoomId);
        List<ChatRoomMemberDO> memberDOs = chatRoomMemberMapper.selectList(memberQuery);

        return memberDOs.stream().map(memberDO -> {
            ChatRoomMemberVO memberVO = new ChatRoomMemberVO();
            BeanUtils.copyProperties(memberDO, memberVO);

            // 查询用户信息
            UserDO user = userInfoMapper.selectById(memberDO.getUserId());
            if (user != null) {
                memberVO.setUserName(user.getNickname());
                memberVO.setAvatarUrl(user.getAvatar());
                memberVO.setOnlineStatus(user.getOnlineStatus()); // 默认离线
            }
            return memberVO;
        }).collect(Collectors.toList());
    }


}
