package com.xs.chat.Utils;


import com.xs.chat.enumeration.VerifyRegexEnum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 需确保 StringTools（含 isEmpty 方法）和 VerifyRegexEnum（含 getRegex 方法）已定义
public class VerifyUtils {

    public static boolean verify(String regex, String value) {
        if (StringUtil.isEmpty(value)) {
            return false;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    public static boolean verify(VerifyRegexEnum regex, String value) {
        return verify(regex.getRegex(), value);
    }
}