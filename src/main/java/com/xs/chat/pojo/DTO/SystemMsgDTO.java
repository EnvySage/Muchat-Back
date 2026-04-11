package com.xs.chat.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class SystemMsgDTO {
    private String content;
    private String type;
    private LocalDateTime sendTime;
}
