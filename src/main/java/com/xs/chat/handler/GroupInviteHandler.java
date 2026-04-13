package com.xs.chat.handler;

import cn.hutool.json.JSONUtil;
import com.xs.chat.enumeration.NoticeTypeEnum;
import com.xs.chat.enumeration.permission.GroupRoleEnum;
import com.xs.chat.event.GroupPermissionUpdateEvent;
import com.xs.chat.mapper.ChatRoomMapper;
import com.xs.chat.mapper.ChatRoomMemberMapper;
import com.xs.chat.mapper.UserInfoMapper;
import com.xs.chat.pojo.DO.ChatRoomDO;
import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.pojo.DO.ChatRoomMemberDO;
import com.xs.chat.pojo.DO.SystemNoticeDO;
import com.xs.chat.service.PermissionService;
import com.xs.chat.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GroupInviteHandler extends AbstractNoticeHandler {

    @Autowired
    private ChatRoomMapper chatRoomMapper;
    @Autowired
    private ChatRoomMemberMapper chatRoomMemberMapper;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public NoticeTypeEnum getType() {
        return NoticeTypeEnum.GROUP_INVITE;
    }

    @Override
    public boolean validate(SystemNoticeDO notice) {
        ChatRoomDO room = chatRoomMapper.selectById(Long.valueOf(notice.getRelatedId()));
        if (room == null || room.getIsActive() == 0) {
            return false;
        }
        // 检查是否已在群中
        ChatRoomMemberDO existing = chatRoomMemberMapper.selectByChatRoomIdAndUserId(
                Long.valueOf(notice.getRelatedId()), notice.getReceiverId());
        return existing == null;
    }

    @Override
    protected void doAccept(SystemNoticeDO notice) {
        Long chatRoomId = Long.valueOf(notice.getRelatedId());
        String userId = notice.getReceiverId();

        ChatRoomMemberDO member = new ChatRoomMemberDO();
        member.setChatRoomId(chatRoomId);
        member.setUserId(userId);
        member.setRole(GroupRoleEnum.MEMBER.getCode());
        chatRoomMemberMapper.insert(member);

        // 清除可能存在的旧角色缓存
        permissionService.cleanRoleCache(chatRoomId, userId);

        // 广播群成员加入通知（群内所有人可见）
        UserDO newUser = userInfoMapper.selectById(userId);
        Map<String, Object> memberData = new HashMap<>();
        memberData.put("userId", userId);
        memberData.put("userName", newUser != null ? newUser.getNickname() : "");
        memberData.put("avatarUrl", newUser != null ? newUser.getAvatar() : "");
        memberData.put("role", GroupRoleEnum.MEMBER.getCode());
        applicationEventPublisher.publishEvent(new GroupPermissionUpdateEvent(this, chatRoomId, "MEMBER_JOIN",
                JSONUtil.toJsonStr(memberData)));

        // 推送 GROUP_INVITE_ACCEPTED 给邀请发起方
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "GROUP_INVITE_ACCEPTED");
        msg.put("chatRoomId", chatRoomId);
        msg.put("acceptedUserId", userId);
        ChatRoomDO room = chatRoomMapper.selectById(chatRoomId);
        if (room != null) {
            msg.put("chatRoomName", room.getName());
        }
        webSocketService.sendToUser(notice.getSenderId(), JSONUtil.toJsonStr(msg));

        // 推送 GROUP_MEMBER_JOINED 给新成员，通知其加入群聊频道
        Map<String, Object> joinMsg = new HashMap<>();
        joinMsg.put("type", "GROUP_MEMBER_JOINED");
        joinMsg.put("chatRoomId", chatRoomId);
        webSocketService.sendToUser(userId, JSONUtil.toJsonStr(joinMsg));

        log.info("群邀请同意: userId={} 加入 chatRoomId={}", userId, chatRoomId);
    }
}
