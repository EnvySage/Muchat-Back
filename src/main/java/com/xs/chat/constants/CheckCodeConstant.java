package com.xs.chat.constants;

public class CheckCodeConstant {
    public static final int DEFAULT_WIDTH = 100;    // 默认宽度
    public static final int DEFAULT_HEIGHT = 40;   // 默认高度
    public static final int DEFAULT_CHAR_COUNT = 5;  // 默认字符数
    public static final int DEFAULT_LINE_COUNT = 20; // 默认干扰线数量
    public static final String DEFAULT_CHAR_SET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"; // 默认字符集（排除易混淆字符）

    public static final String CHECK_CODE_KEY = "checkCode";
    public static final String CHECK_CODE_EMAIL_KEY = "checkCodeEmail";
}
