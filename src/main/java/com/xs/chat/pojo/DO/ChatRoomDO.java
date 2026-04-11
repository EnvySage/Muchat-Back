package com.xs.chat.pojo.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xs.chat.enumeration.ChatRoomEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_room")
public class ChatRoomDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String type;
    private String creatorId;
    private LocalDateTime createdAt;
    private String description;
    private String avatarUrl;
    private Integer isActive;
    private Integer isPin;
}
