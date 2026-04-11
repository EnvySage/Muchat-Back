package com.xs.chat.pojo.DO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_room_member")
public class ChatRoomMemberDO {
    private Long id;
    private Long chatRoomId;
    private String userId;
    private String role;
    private LocalDateTime joinedAt;
    private Long lastReadMessageId;
    private Integer isMuted;
    private Integer isVisible;
    private String roomName;
}
