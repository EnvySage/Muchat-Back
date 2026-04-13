package com.xs.chat.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xs.chat.Exception.BusinessException;
import com.xs.chat.context.BaseContext;
import com.xs.chat.enumeration.NoticeStatusEnum;
import com.xs.chat.enumeration.NoticeTypeEnum;
import com.xs.chat.enumeration.ResponseCodeEnum;
import com.xs.chat.mapper.ContactMapper;
import com.xs.chat.mapper.SystemNoticeMapper;
import com.xs.chat.mapper.UserInfoMapper;
import com.xs.chat.pojo.DO.ContactsDO;
import com.xs.chat.pojo.DO.SystemNoticeDO;
import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.pojo.DTO.ContactsDTO;
import com.xs.chat.pojo.VO.ContactsVO;
import com.xs.chat.service.ContactService;
import com.xs.chat.service.NoticeService;
import com.xs.chat.service.WebSocketService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ContactImpl implements ContactService {
    @Resource
    private ContactMapper contactsMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private NoticeService noticeService;
    @Resource
    private SystemNoticeMapper systemNoticeMapper;
    @Resource
    private WebSocketService webSocketService;

    @Override
    public List<ContactsVO> getAllById() {
        LambdaQueryWrapper<ContactsDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContactsDO::getUserId, BaseContext.getCurrentId());
        Map<String, String> map = contactsMapper.selectList(queryWrapper).stream().collect(Collectors.toMap(ContactsDO::getContactId, ContactsDO::getAlias));
        List<String> idList = map.keySet().stream().toList();
        if (idList.isEmpty()) {
            return List.of();
        }
        List<ContactsVO> contactsVOList = userInfoMapper.selectBatchIds(idList).stream().map(item -> {
            ContactsVO contactsVO = new ContactsVO();
            contactsVO.setUserId(BaseContext.getCurrentId());
            contactsVO.setContactId(item.getId());
            contactsVO.setContactNickname(item.getNickname());
            contactsVO.setContactAvatar(item.getAvatar());
            contactsVO.setContactDescription(item.getDescription());
            contactsVO.setAlias(map.get(item.getId()));
            contactsVO.setContactOnlineStatus(item.getOnlineStatus());
            contactsVO.setLastLoginAt(item.getLastLoginAt());
            contactsVO.setEmail(item.getEmail());
            return contactsVO;
        }).collect(Collectors.toList());
        log.info("查询联系人成功:{}", contactsVOList);
        return contactsVOList;
    }

    /**
     * 单向添加一条联系人记录（已存在则跳过）
     */
    private void addContactOneWay(String userId, String contactId, String alias) {
        // 检查是否已存在
        LambdaQueryWrapper<ContactsDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContactsDO::getUserId, userId)
                .eq(ContactsDO::getContactId, contactId);
        if (contactsMapper.selectCount(wrapper) > 0) {
            log.info("联系人已存在，跳过: userId={}, contactId={}", userId, contactId);
            return;
        }

        ContactsDO contactsDO = new ContactsDO();
        contactsDO.setUserId(userId);
        contactsDO.setContactId(contactId);
        if (alias == null || alias.isEmpty()) {
            UserDO contactUser = userInfoMapper.selectById(contactId);
            contactsDO.setAlias(contactUser != null ? contactUser.getNickname() : contactId);
        } else {
            contactsDO.setAlias(alias);
        }
        int row = contactsMapper.insert(contactsDO);
        if (row > 0) {
            log.info("添加联系人成功: userId={}, contactId={}", userId, contactId);
        } else {
            log.warn("添加联系人失败: userId={}, contactId={}", userId, contactId);
        }
    }

    /**
     * 双向删除联系人
     */
    @Override
    public void deleteContact(String contactId) {
        String currentUserId = BaseContext.getCurrentId();

        // 删除自己到对方的记录
        deleteContactOneWay(currentUserId, contactId);
        // 删除对方到自己的记录
        deleteContactOneWay(contactId, currentUserId);

        // WS推送 FRIEND_REMOVED 给对方，刷新其联系人列表
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "FRIEND_REMOVED");
        msg.put("friendId", currentUserId);
        webSocketService.sendToUser(contactId, cn.hutool.json.JSONUtil.toJsonStr(msg));
    }

    private void deleteContactOneWay(String userId, String contactId) {
        LambdaQueryWrapper<ContactsDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContactsDO::getUserId, userId)
                .eq(ContactsDO::getContactId, contactId);
        int row = contactsMapper.delete(queryWrapper);
        if (row > 0) {
            log.info("删除联系人成功: userId={}, contactId={}", userId, contactId);
        } else {
            log.warn("删除联系人不存在: userId={}, contactId={}", userId, contactId);
        }
    }

    @Override
    public boolean updateContact(ContactsDTO contactsDTO) {
        LambdaQueryWrapper<ContactsDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContactsDO::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(ContactsDO::getContactId, contactsDTO.getContactId());
        ContactsDO contactsDO = new ContactsDO();
        BeanUtils.copyProperties(contactsDTO, contactsDO);
        int row = contactsMapper.update(contactsDO, queryWrapper);
        if (row > 0) {
            log.info("更新联系人成功:{}", contactsDO);
            return true;
        }
        log.warn("更新联系人失败:{}", contactsDO);
        return false;
    }

    @Override
    public void sendFriendRequest(String contactId) {
        String currentUserId = BaseContext.getCurrentId();

        // 不能加自己为好友
        if (currentUserId.equals(contactId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "不能添加自己为好友");
        }

        // 检查目标用户是否存在
        UserDO targetUser = userInfoMapper.selectById(contactId);
        if (targetUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "目标用户不存在");
        }

        // 双向检查是否已经是好友（任意一方有记录就算好友）
        if (isFriend(currentUserId, contactId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "对方已经是你的好友");
        }

        // 检查对方是否已向你发送过待处理的好友邀请（如果有，直接互相添加好友）
        LambdaQueryWrapper<SystemNoticeDO> reverseNoticeWrapper = new LambdaQueryWrapper<>();
        reverseNoticeWrapper.eq(SystemNoticeDO::getSenderId, contactId)
                .eq(SystemNoticeDO::getReceiverId, currentUserId)
                .eq(SystemNoticeDO::getType, NoticeTypeEnum.FRIEND_INVITE.getCode())
                .in(SystemNoticeDO::getStatus, NoticeStatusEnum.UNREAD.getCode(), NoticeStatusEnum.READ.getCode());
        SystemNoticeDO reverseNotice = systemNoticeMapper.selectOne(reverseNoticeWrapper);
        if (reverseNotice != null) {
            // 对方已向你发过邀请，直接互相添加好友，并将对方邀请标记为已同意
            addContactOneWay(currentUserId, contactId, targetUser.getNickname());
            UserDO currentUser = userInfoMapper.selectById(currentUserId);
            addContactOneWay(contactId, currentUserId, currentUser != null ? currentUser.getNickname() : null);

            // 将对方的邀请通知标记为已同意
            SystemNoticeDO update = new SystemNoticeDO();
            update.setId(reverseNotice.getId());
            update.setStatus(NoticeStatusEnum.ACCEPTED.getCode());
            update.setUpdatedAt(LocalDateTime.now());
            systemNoticeMapper.updateById(update);

            log.info("双向邀请匹配，直接互加好友: userId={}, contactId={}", currentUserId, contactId);
            return;
        }

        // 检查是否已有待处理的好友邀请（自己发给对方的）
        LambdaQueryWrapper<SystemNoticeDO> noticeWrapper = new LambdaQueryWrapper<>();
        noticeWrapper.eq(SystemNoticeDO::getSenderId, currentUserId)
                .eq(SystemNoticeDO::getReceiverId, contactId)
                .eq(SystemNoticeDO::getType, NoticeTypeEnum.FRIEND_INVITE.getCode())
                .in(SystemNoticeDO::getStatus, NoticeStatusEnum.UNREAD.getCode(), NoticeStatusEnum.READ.getCode());
        if (systemNoticeMapper.selectCount(noticeWrapper) > 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "已发送过好友邀请，请等待对方处理");
        }

        UserDO sender = userInfoMapper.selectById(currentUserId);
        String senderName = sender != null ? sender.getNickname() : "未知用户";

        SystemNoticeDO notice = new SystemNoticeDO();
        notice.setReceiverId(contactId);
        notice.setSenderId(currentUserId);
        notice.setType(NoticeTypeEnum.FRIEND_INVITE.getCode());
        notice.setTitle(senderName + " 请求添加你为好友");
        notice.setRelatedId(currentUserId);
        notice.setExtraData(cn.hutool.json.JSONUtil.toJsonStr(Map.of(
                "senderName", senderName,
                "senderAvatar", sender != null && sender.getAvatar() != null ? sender.getAvatar() : ""
        )));
        notice.setStatus(NoticeStatusEnum.UNREAD.getCode());
        notice.setExpiredAt(LocalDateTime.now().plusDays(7));
        noticeService.sendNotice(notice);

        log.info("发送好友邀请: senderId={}, contactId={}", currentUserId, contactId);
    }

    /**
     * 检查两人是否为好友关系（双向任一存在即视为好友）
     */
    private boolean isFriend(String userId, String contactId) {
        LambdaQueryWrapper<ContactsDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContactsDO::getUserId, userId)
                .eq(ContactsDO::getContactId, contactId);
        return contactsMapper.selectCount(wrapper) > 0;
    }
}
