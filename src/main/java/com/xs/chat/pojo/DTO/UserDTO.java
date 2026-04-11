package com.xs.chat.pojo.DTO;

import com.xs.chat.Annotation.VerifyParam;
import com.xs.chat.enumeration.VerifyRegexEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    @VerifyParam(required = true, min = 2, max = 20, verifyRegex = VerifyRegexEnum.COMMON)
    private String nickname;
    @VerifyParam(required = true, min = 8, max = 18, verifyRegex = VerifyRegexEnum.PASSWORD)
    private String password;
    @VerifyParam(max = 200)
    private String description;
    @VerifyParam(required = true, verifyRegex = VerifyRegexEnum.EMAIL)
    private String email;
    @VerifyParam(min = 4, max = 6)
    private String loginCode;
    @VerifyParam(min = 4, max = 6)
    private String emailCode;
}
