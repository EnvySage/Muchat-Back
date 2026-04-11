package com.xs.chat.pojo.DTO;

import com.xs.chat.Annotation.VerifyParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomMemberDTO {
    @VerifyParam(required = true)
    private Long chatRoomId;
    private String userId;
    private String role;
    private Long lastReadMessageId;
    private String roomName;
    private List<String> userIdList;

    private Integer isMuted;
}
