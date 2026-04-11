package com.xs.chat.Aspect;

import com.xs.chat.Annotation.RequirePermission;
import com.xs.chat.Exception.BusinessException;
import com.xs.chat.Utils.StringUtil;
import com.xs.chat.context.BaseContext;
import com.xs.chat.enumeration.ResponseCodeEnum;
import com.xs.chat.enumeration.permission.GroupPermissionEnum;
import com.xs.chat.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class PermissionAspect {
    @Autowired
    private PermissionService permissionService;

    @Around("@annotation(com.xs.chat.Annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint point) throws Throwable{
        //获取注解
        Method method= ((MethodSignature)point.getSignature()).getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);
        GroupPermissionEnum requirePermission = annotation.value();
        String chatRoomIdField = annotation.chatRoomId();

        String currentUserId = BaseContext.getCurrentId();
        if (StringUtil.isEmpty(currentUserId)){
            throw new BusinessException(ResponseCodeEnum.CODE_401,"未登录");
        }

        Long chatRoomId = extractChatRoomId(point,chatRoomIdField);
        if (chatRoomId==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600,"chatRoomId为空");
        }

        permissionService.checkPermission(chatRoomId,currentUserId,requirePermission);
        Object result = point.proceed();
        return result;
    }

    private Long extractChatRoomId(ProceedingJoinPoint point,String filedName){
        Object[] args = point.getArgs();
        if (args.length==0 || args ==null){
            return null;
        }

        for (Object arg : args){
            if (arg==null) continue;

            if (arg instanceof Long){
                return (Long) arg;
            }

            try {
                Field field =arg.getClass().getDeclaredField(filedName);
                field.setAccessible(true);
                Object value = field.get(arg);
                if (value instanceof Long){
                    return (Long) value;
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
