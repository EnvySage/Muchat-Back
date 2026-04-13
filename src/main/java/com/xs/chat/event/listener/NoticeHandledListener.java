package com.xs.chat.event.listener;

import cn.hutool.json.JSONUtil;
import com.xs.chat.event.NoticeHandledEvent;
import com.xs.chat.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class NoticeHandledListener {

    @Autowired
    private WebSocketService webSocketService;

    @Async
    @EventListener(classes = NoticeHandledEvent.class)
    public void onNoticeHandled(NoticeHandledEvent event) {
        log.info("通知处理事件: noticeId={}, senderId={}, action={}", event.getNoticeId(), event.getSenderId(), event.getAction());

        // 构建推送数据
        Map<String, Object> data = new HashMap<>();
        data.put("type", "NOTICE_HANDLED");
        data.put("noticeId", event.getNoticeId());
        data.put("noticeType", event.getType());
        data.put("action", event.getAction());
        data.put("handlerId", event.getReceiverId());  // 处理人的ID
        data.put("title", event.getTitle());

        // 推送给通知发起方（sender）
        webSocketService.sendToUser(event.getSenderId(), JSONUtil.toJsonStr(data));
    }
}
