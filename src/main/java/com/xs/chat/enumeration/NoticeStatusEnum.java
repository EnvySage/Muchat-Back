package com.xs.chat.enumeration;

import lombok.Getter;

@Getter
public enum NoticeStatusEnum {
    UNREAD("UNREAD", "未读"),
    READ("READ", "已读"),
    ACCEPTED("ACCEPTED", "已同意"),
    REJECTED("REJECTED", "已拒绝"),
    EXPIRED("EXPIRED", "已过期");

    private final String code;
    private final String desc;

    NoticeStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static NoticeStatusEnum fromCode(String code) {
        for (NoticeStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
