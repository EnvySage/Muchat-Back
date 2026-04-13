package com.xs.chat.pojo.DTO;

import com.xs.chat.Annotation.VerifyParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoticeHandleDTO {
    @VerifyParam(required = true)
    private Long noticeId;
    @VerifyParam(required = true)
    private String action; // ACCEPT / REJECT
}
