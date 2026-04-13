package com.xs.chat.event.listener;

import cn.hutool.json.JSONUtil;
import com.xs.chat.event.NoticeSentEvent;
import com.xs.chat.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoticeSentListener {

    @Autowired
    private WebSocketService webSocketService;

    @Async
    @EventListener(classes = NoticeSentEvent.class)
    public void onNoticeSent(NoticeSentEvent event) {
        log.info("通知发送事件: noticeId={}, receiverId={}, type={}", event.getNoticeId(), event.getReceiverId(), event.getType());
        webSocketService.sendNoticePush(event.getReceiverId(), event.getNoticeId(), event.getType(), event.getTitle());
    }
}
