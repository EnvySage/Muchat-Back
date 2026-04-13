package com.xs.chat.pojo.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("system_notice")
public class SystemNoticeDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String receiverId;
    private String senderId;
    private String type;
    private String title;
    private String content;
    private String relatedId;
    private String extraData;
    private String status;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
