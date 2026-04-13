package com.xs.chat.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageVO {
    private Long id;
    private Long chatRoomId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private String contentType;
    private String fileName;
    private Long fileSize;
    private Long sentAt;
}
