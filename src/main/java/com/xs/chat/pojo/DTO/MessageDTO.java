package com.xs.chat.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    private Long chatRoomId; //房间ID
    private String type; //消息类型
    private String senderId;//发生者id
    private String content; //内容
    private Long messageId; //消息id
    private String contentType; //消息类型（文本，图片等）
    private String senderName;
    private String senderAvatar;
    private Long sentAt;
}
