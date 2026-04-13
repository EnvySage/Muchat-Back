package com.xs.chat.pojo.DO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class UserDO {
    private String id;
    private String nickname;
    private String passwordHash;
    private String email;
    private String avatar;
    private Integer status;
    private String description;
    private Integer onlineStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate lastLoginAt;

}
