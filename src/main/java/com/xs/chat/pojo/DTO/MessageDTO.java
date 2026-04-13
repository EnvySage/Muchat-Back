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
    private String type; //消息类型 (JOIN_GROUP / LEAVE_GROUP / GROUP)
    private String senderId;//发生者id
    private String content; //内容（文本为纯文本，非文本为JSON字符串）
    private Long messageId; //消息id
    private String contentType; //消息内容类型，参见 MessageContentTypeEnum (TEXT / IMAGE / VIDEO / PDF / WORD / EXCEL / ZIP / FILE)
    private String fileName; //文件名（仅文件/图片/视频类消息需要）
    private Long fileSize; //文件大小，单位字节（仅文件/图片/视频类消息需要）
    private String senderName;
    private String senderAvatar;
    private Long sentAt;
}
