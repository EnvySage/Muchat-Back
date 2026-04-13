package com.xs.chat.pojo.DTO;

import lombok.Data;

@Data
public class NoticeQueryDTO {
    private String type;
    private String status;
    private Integer page = 1;
    private Integer size = 20;
}
