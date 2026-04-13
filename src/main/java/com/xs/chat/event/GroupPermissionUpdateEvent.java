package com.xs.chat.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class GroupPermissionUpdateEvent extends ApplicationEvent {
    private final Long chatRoomId;
    private final String action;
    private final String data;

    public GroupPermissionUpdateEvent(Object source, Long chatRoomId, String action, String data) {
        super(source);
        this.chatRoomId = chatRoomId;
        this.action = action;
        this.data = data;
    }
}
