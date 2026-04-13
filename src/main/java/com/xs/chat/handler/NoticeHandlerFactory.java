package com.xs.chat.handler;

import com.xs.chat.Exception.BusinessException;
import com.xs.chat.enumeration.NoticeTypeEnum;
import com.xs.chat.enumeration.ResponseCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class NoticeHandlerFactory {

    private final Map<NoticeTypeEnum, NoticeHandler> handlerMap = new HashMap<>();

    public NoticeHandlerFactory(List<NoticeHandler> handlers) {
        handlers.forEach(h -> handlerMap.put(h.getType(), h));
        log.info("注册通知处理器: {}", handlerMap.keySet());
    }

    public NoticeHandler getHandler(NoticeTypeEnum type) {
        NoticeHandler handler = handlerMap.get(type);
        if (handler == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "不支持的通知类型: " + type);
        }
        return handler;
    }

    public boolean hasHandler(NoticeTypeEnum type) {
        return handlerMap.containsKey(type);
    }
}
