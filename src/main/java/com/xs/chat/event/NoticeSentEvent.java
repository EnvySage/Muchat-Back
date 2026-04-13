package com.xs.chat.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NoticeSentEvent extends ApplicationEvent {
    private final Long noticeId;
    private final String receiverId;
    private final String type;
    private final String title;

    public NoticeSentEvent(Object source, Long noticeId, String receiverId, String type, String title) {
        super(source);
        this.noticeId = noticeId;
        this.receiverId = receiverId;
        this.type = type;
        this.title = title;
    }
}
