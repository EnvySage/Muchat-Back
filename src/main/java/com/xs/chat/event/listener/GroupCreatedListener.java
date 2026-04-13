package com.xs.chat.event.listener;

import com.xs.chat.event.GroupCreatedEvent;
import com.xs.chat.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GroupCreatedListener {
    @Autowired
    private WebSocketService webSocketService;

    @Async
    @EventListener(classes = GroupCreatedEvent.class)
    public void createGroup(GroupCreatedEvent event){
        log.info("发送创建群组广播: {}, 被邀请人: {}", event.getChatRoomVO(), event.getInvitedUserIds());
        webSocketService.groupCreatedBroadcast(event.getChatRoomVO(), event.getInvitedUserIds());
    }
}
