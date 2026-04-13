package com.xs.chat.handler;

import com.xs.chat.pojo.DO.SystemNoticeDO;

public interface NoticeHandler {
    com.xs.chat.enumeration.NoticeTypeEnum getType();

    void handleAccept(SystemNoticeDO notice);

    void handleReject(SystemNoticeDO notice);

    boolean validate(SystemNoticeDO notice);
}
