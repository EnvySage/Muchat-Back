package com.xs.chat.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoticeVO {
    private Long id;
    private String receiverId;
    private String senderId;
    private String senderNickname;
    private String senderAvatar;
    private String type;
    private String typeDesc;
    private String title;
    private String content;
    private String relatedId;
    private String extraData;
    private String status;
    private String statusDesc;
    private Boolean needAction;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
}
