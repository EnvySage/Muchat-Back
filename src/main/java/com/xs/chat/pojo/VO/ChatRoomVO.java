package com.xs.chat.pojo.VO;

import com.xs.chat.enumeration.ChatRoomEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomVO {
    private Long id;
    private String name;
    private String type;
    private String creatorId;
    private LocalDateTime createdAt;
    private String description;
    private String avatarUrl;
    private Integer isPin;
    private Integer isActive;
    private String lastMessageContent;
    private String lastMessageSenderId;
    private String lastMessageSenderName;
    private Long lastMessageSentAt;
    private Long unreadCount;
    private List<ChatRoomMemberVO> members;
    private Integer memberCount;
}
