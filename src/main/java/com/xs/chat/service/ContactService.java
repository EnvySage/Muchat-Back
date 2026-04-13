package com.xs.chat.service;

import com.xs.chat.pojo.DTO.ContactsDTO;
import com.xs.chat.pojo.VO.ContactsVO;

import java.util.List;

public interface ContactService {
    List<ContactsVO> getAllById();

    void deleteContact(String contactId);

    boolean updateContact(ContactsDTO contactsDTO);

    /**
     * 发送好友邀请通知
     * @param contactId 目标用户ID
     */
    void sendFriendRequest(String contactId);
}
