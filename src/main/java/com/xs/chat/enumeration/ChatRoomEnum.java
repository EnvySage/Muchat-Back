package com.xs.chat.enumeration;



public enum ChatRoomEnum {
    PUBLIC("PUBLIC","公共聊天室"),
    PRIVATE("PRIVATE","私有聊天室"),
    GROUP("GROUP","群聊");
    private String code;
    private String message;
    ChatRoomEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
    public String getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
    public static ChatRoomEnum getMessageByCode(String code) {
        for (ChatRoomEnum value : ChatRoomEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
