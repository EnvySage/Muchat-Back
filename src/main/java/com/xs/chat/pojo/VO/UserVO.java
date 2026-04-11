package com.xs.chat.pojo.VO;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private String id;
    private String nickname;
    private String avatar;
    private String email;
    private Integer status;
    private String description;
    private String token;
    private LocalDateTime createdAt;
    private LocalDate lastLoginAt;
    private Integer onlineStatus;
}
