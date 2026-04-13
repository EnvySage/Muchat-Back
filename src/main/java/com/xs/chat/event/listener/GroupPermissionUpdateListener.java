package com.xs.chat.event.listener;

import com.xs.chat.event.GroupPermissionUpdateEvent;
import com.xs.chat.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GroupPermissionUpdateListener {
    @Autowired
    private WebSocketService webSocketService;

    @Async
    @EventListener(classes = GroupPermissionUpdateEvent.class)
    public void onGroupPermissionUpdate(GroupPermissionUpdateEvent event) {
        log.info("群权限变更广播: chatRoomId={}, action={}", event.getChatRoomId(), event.getAction());
        webSocketService.groupPermissionBroadcast(event.getChatRoomId(), event.getAction(), event.getData());
    }
}
