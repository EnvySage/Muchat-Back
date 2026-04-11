package com.xs.chat.Annotation;


import com.xs.chat.enumeration.VerifyRegexEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface VerifyParam {
    int min() default -1;
    int max() default -1;
    boolean required() default false;
    VerifyRegexEnum verifyRegex() default VerifyRegexEnum.NO;
}
