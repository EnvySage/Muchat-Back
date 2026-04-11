package com.xs.chat.pojo.DTO;

import lombok.Data;

@Data
public class AuthDTO {
    private String type = "AUTH";
    private String token;
}
