package com.xs.chat.pojo.VO;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ContactsVO {
    private String userId;
    private String contactId;
    private String contactNickname;
    private String contactAvatar;
    private String contactDescription;
    private Integer contactOnlineStatus;
    private LocalDate lastLoginAt;
    private String email;
    private String alias;
}
