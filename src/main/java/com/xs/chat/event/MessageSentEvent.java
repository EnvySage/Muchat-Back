package com.xs.chat.event;

import com.xs.chat.pojo.DTO.MessageDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class MessageSentEvent extends ApplicationEvent {
    private final MessageDTO messageDTO;
    public MessageSentEvent(Object source, MessageDTO messageDTO) {
        super(source);
        this.messageDTO = messageDTO;
    }
}
