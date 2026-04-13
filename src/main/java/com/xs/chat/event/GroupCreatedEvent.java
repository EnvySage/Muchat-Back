package com.xs.chat.event;

import com.xs.chat.pojo.VO.ChatRoomVO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class GroupCreatedEvent extends ApplicationEvent {
    private final ChatRoomVO chatRoomVO;
    private final List<String> invitedUserIds;

    public GroupCreatedEvent(Object source, ChatRoomVO chatRoomVO) {
        this(source, chatRoomVO, null);
    }

    public GroupCreatedEvent(Object source, ChatRoomVO chatRoomVO, List<String> invitedUserIds) {
        super(source);
        this.chatRoomVO = chatRoomVO;
        this.invitedUserIds = invitedUserIds;
    }
}
