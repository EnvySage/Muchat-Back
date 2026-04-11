package com.xs.chat.pojo.DTO;

import com.xs.chat.Annotation.VerifyParam;
import com.xs.chat.enumeration.VerifyRegexEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailCodeDTO {
    @VerifyParam(required = true, verifyRegex = VerifyRegexEnum.EMAIL)
    private String email;
    @VerifyParam(required = true, min = 4, max = 6)
    private String checkCode;
    @VerifyParam(required = true)
    private String type;
}
