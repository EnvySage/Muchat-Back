package com.xs.chat.pojo.DTO;

import lombok.Data;

@Data
public class AuthResultDTO {
    private String type = "AUTH_RESULT";
    private Integer code;
    private String message;
    private String userId;
}
