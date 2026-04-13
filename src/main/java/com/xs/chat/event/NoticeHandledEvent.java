package com.xs.chat.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 通知被处理（同意/拒绝）事件，用于WS推送给通知发起方
 */
@Getter
public class NoticeHandledEvent extends ApplicationEvent {

    private final Long noticeId;
    private final String senderId;       // 通知发起方（原通知的senderId）
    private final String receiverId;     // 通知接收方（处理人）
    private final String type;           // 通知类型
    private final String action;         // ACCEPT / REJECT
    private final String title;

    public NoticeHandledEvent(Object source, Long noticeId, String senderId, String receiverId,
                              String type, String action, String title) {
        super(source);
        this.noticeId = noticeId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
        this.action = action;
        this.title = title;
    }
}
