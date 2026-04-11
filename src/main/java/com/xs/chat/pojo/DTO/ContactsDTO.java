package com.xs.chat.pojo.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContactsDTO {
    private String userId;
    private String contactNickname;
    private String contactId;
    private String alias;
}
