package com.xs.chat.event;

import com.xs.chat.pojo.DO.ChatRoomDO;
import com.xs.chat.pojo.VO.ChatRoomVO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class GroupCreatedEvent extends ApplicationEvent {
    private final ChatRoomVO chatRoomVO;
    public GroupCreatedEvent(Object source, ChatRoomVO chatRoomVO) {
        super(source);
        this.chatRoomVO = chatRoomVO;
    }
}
