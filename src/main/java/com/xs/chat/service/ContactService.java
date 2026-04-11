package com.xs.chat.service;

import com.xs.chat.pojo.DTO.ContactsDTO;
import com.xs.chat.pojo.VO.ContactsVO;

import java.util.List;

public interface ContactService {
    List<ContactsVO> getAllById();

    void addContact(ContactsDTO contactsDTO);

    void deleteContact(String contactId);

    boolean updateContact(ContactsDTO contactsDTO);
}
