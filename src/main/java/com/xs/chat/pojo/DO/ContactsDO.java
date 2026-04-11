package com.xs.chat.pojo.DO;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@TableName("user_contacts")
public class ContactsDO {
    private Long id;
    private String userId;
    private String contactId;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String alias;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
