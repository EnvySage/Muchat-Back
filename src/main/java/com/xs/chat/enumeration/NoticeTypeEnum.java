package com.xs.chat.enumeration;

import lombok.Getter;

@Getter
public enum NoticeTypeEnum {
    SYSTEM_NOTICE("SYSTEM_NOTICE", "系统通知", false),
    GROUP_INVITE("GROUP_INVITE", "群邀请", true),
    FRIEND_INVITE("FRIEND_INVITE", "好友邀请", true);

    private final String code;
    private final String desc;
    private final boolean needAction;

    NoticeTypeEnum(String code, String desc, boolean needAction) {
        this.code = code;
        this.desc = desc;
        this.needAction = needAction;
    }

    public static NoticeTypeEnum fromCode(String code) {
        for (NoticeTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
