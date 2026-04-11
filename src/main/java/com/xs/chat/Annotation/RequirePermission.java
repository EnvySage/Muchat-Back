package com.xs.chat.Annotation;

import com.xs.chat.enumeration.permission.GroupPermissionEnum;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    GroupPermissionEnum value();
    String chatRoomId() default "chatRoomId";
}
