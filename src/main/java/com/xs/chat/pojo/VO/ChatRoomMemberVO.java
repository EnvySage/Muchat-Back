package com.xs.chat.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomMemberVO {
    private Long id;
    private Long chatRoomId;
    private String userId;
    private String userName;
    private String avatarUrl;
    private Integer onlineStatus;
    private String role;
    private LocalDateTime joinedAt;
    private Long lastReadMessageId;
    private Integer isMuted;
    private Integer isVisible;
    private Integer unreadCount;
    private String roomName;
}