package com.xs.chat.enumeration;

public enum VerifyRegexEnum {
    // 不校验
    NO("", "不校验"),
    // IP 地址
    IP("((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)", "IP地址"),
    // 正整数
    POSITIVE_INTEGER("^[1-9]\\d*$", "正整数"),
    // 由数字、26个英文字母或者下划线组成的字符串
    NUMBER_LETTER_UNDER_LINE("^\\w+$", "由数字、26个英文字母或者下划线组成的字符串"),
    // 邮箱
    EMAIL("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", "邮箱"),
    // 手机号码（中国大陆）
    PHONE("^(1[3-9])\\d{9}$", "手机号码"),
    // 数字、字母、中文、下划线
    COMMON("^[\u4e00-\u9fa5a-zA-Z0-9_]+$", "数字、字母、中文、下划线"),
    // 密码（仅数字+字母，8-18位；若需特殊字符需调整正则）
    PASSWORD("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,18}$", "只能是数字、字母，8-18位"),
    // 账户（字母开头，由数字、英文字母或下划线组成）
    ACCOUNT("^[a-zA-Z][a-zA-Z0-9_]{1,}$", "字母开头，由数字、英文字母或者下划线组成"),
    // 金额（支持整数或最多两位小数）
    MONEY("^[0-9]+(\\.[0-9]{1,2})?$", "金额");

    private final String regex;
    private final String desc;

    VerifyRegexEnum(String regex, String desc) {
        this.regex = regex;
        this.desc = desc;
    }

    public String getRegex() {
        return regex;
    }

    public String getDesc() {
        return desc;
    }
}