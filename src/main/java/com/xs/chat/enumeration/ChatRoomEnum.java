package com.xs.chat.enumeration;



public enum ChatRoomEnum {
    PUBLIC(0,"公共聊天室"),
    PRIVATE(1,"私有聊天室"),
    GROUP(2,"群聊");
    private Integer code;
    private String message;
    ChatRoomEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    public Integer getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
    public static ChatRoomEnum getMessageByCode(Integer code) {
        for (ChatRoomEnum value : ChatRoomEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
