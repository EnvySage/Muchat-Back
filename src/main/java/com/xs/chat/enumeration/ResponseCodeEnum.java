package com.xs.chat.enumeration;

import lombok.Getter;


@Getter
public enum ResponseCodeEnum {
    CODE_200(200, "成功"),
    CODE_500(500, "服务器内部错误"),
    CODE_600(600, "参数错误"),
    CODE_401(401, "未授权"),
    CODE_403(403, "禁止访问");

    private final int code;
    private final String msg;

    ResponseCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}