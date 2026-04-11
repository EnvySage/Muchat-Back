package com.xs.chat.pojo.DTO;

import com.xs.chat.Annotation.VerifyParam;
import com.xs.chat.enumeration.ChatRoomEnum;
import com.xs.chat.enumeration.VerifyRegexEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomDTO {
    private Long chatRoomId;
    @VerifyParam(required = true, min = 1, max = 30, verifyRegex = VerifyRegexEnum.COMMON)
    private String name;
    private String creatorId;
    @VerifyParam(max = 200)
    private String description;
    private String type;
    private String avatarUrl;
    private Integer isPin;
    private List<String> memberIdList;
}
