package com.xs.chat.pojo.DO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("message")
public class MessageDO {
    private Long id;
    private Long chatRoomId;
    private String senderId;
    private String content;
    private String contentType;
    private Long sentAt;
    private Integer isDeleted;
}
