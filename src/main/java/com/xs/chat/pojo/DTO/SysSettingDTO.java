package com.xs.chat.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysSettingDTO {
    private String RegisterEmailTitle = "欢迎来到聊天室";
    private String RegisterEmailContent = "欢迎使用聊天室，此次验证码为%s，有效期五分钟，请勿回复此邮件";
}
