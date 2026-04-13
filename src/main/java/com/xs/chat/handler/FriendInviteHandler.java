package com.xs.chat.handler;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xs.chat.enumeration.NoticeTypeEnum;
import com.xs.chat.mapper.ContactMapper;
import com.xs.chat.mapper.UserInfoMapper;
import com.xs.chat.pojo.DO.ContactsDO;
import com.xs.chat.pojo.DO.SystemNoticeDO;
import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class FriendInviteHandler extends AbstractNoticeHandler {

    @Autowired
    private ContactMapper contactMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private WebSocketService webSocketService;

    @Override
    public NoticeTypeEnum getType() {
        return NoticeTypeEnum.FRIEND_INVITE;
    }

    @Override
    public boolean validate(SystemNoticeDO notice) {
        UserDO sender = userInfoMapper.selectById(notice.getSenderId());
        return sender != null;
    }

    @Override
    protected void doAccept(SystemNoticeDO notice) {
        String receiverId = notice.getReceiverId();
        String senderId = notice.getSenderId();

        // 双向添加联系人（跳过已存在的记录）
        addContactIfNotExists(receiverId, senderId);
        addContactIfNotExists(senderId, receiverId);

        // 推送 FRIEND_ADDED 给双方，刷新联系人列表
        UserDO senderUser = userInfoMapper.selectById(senderId);
        UserDO receiverUser = userInfoMapper.selectById(receiverId);

        // 告知发起方：对方已同意，新增联系人
        Map<String, Object> senderMsg = new HashMap<>();
        senderMsg.put("type", "FRIEND_ADDED");
        senderMsg.put("friendId", receiverId);
        senderMsg.put("friendNickname", receiverUser != null ? receiverUser.getNickname() : "");
        senderMsg.put("friendAvatar", receiverUser != null ? receiverUser.getAvatar() : "");
        webSocketService.sendToUser(senderId, JSONUtil.toJsonStr(senderMsg));

        // 告知接受方：已成功添加好友（刷新列表）
        Map<String, Object> receiverMsg = new HashMap<>();
        receiverMsg.put("type", "FRIEND_ADDED");
        receiverMsg.put("friendId", senderId);
        receiverMsg.put("friendNickname", senderUser != null ? senderUser.getNickname() : "");
        receiverMsg.put("friendAvatar", senderUser != null ? senderUser.getAvatar() : "");
        webSocketService.sendToUser(receiverId, JSONUtil.toJsonStr(receiverMsg));

        log.info("好友邀请同意: receiverId={} 与 senderId={} 互加好友", receiverId, senderId);
    }

    private void addContactIfNotExists(String userId, String contactId) {
        // 先检查是否已存在
        LambdaQueryWrapper<ContactsDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContactsDO::getUserId, userId)
                .eq(ContactsDO::getContactId, contactId);
        if (contactMapper.selectCount(wrapper) > 0) {
            log.info("联系人已存在，跳过: userId={}, contactId={}", userId, contactId);
            return;
        }
        ContactsDO contactsDO = new ContactsDO();
        contactsDO.setUserId(userId);
        contactsDO.setContactId(contactId);
        UserDO contactUser = userInfoMapper.selectById(contactId);
        if (contactUser != null) {
            contactsDO.setAlias(contactUser.getNickname());
        }
        contactMapper.insert(contactsDO);
    }
}
