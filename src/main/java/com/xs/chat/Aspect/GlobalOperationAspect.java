package com.xs.chat.Aspect;

import com.xs.chat.Annotation.GlobalInterceptor;
import com.xs.chat.Annotation.VerifyParam;
import com.xs.chat.Exception.BusinessException;
import com.xs.chat.Utils.StringUtil;
import com.xs.chat.Utils.VerifyUtils;
import com.xs.chat.enumeration.ResponseCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;



@Aspect
@Slf4j
@Component("globalOperationAspect")
public class GlobalOperationAspect {
    public static final String[] TYPE_BASE = new String[]{
            "java.lang.String",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Boolean",
            "java.lang.Float",
            "java.lang.Double",
            "java.lang.Byte",
            "java.lang.Short",
            "java.lang.Character",
            "int",
            "long",
            "boolean",
            "float",
            "double",
            "byte",
            "short",
            "char"
    };
    @Pointcut("@annotation(com.xs.chat.Annotation.GlobalInterceptor)")
    private void requestInterceptor(){}

    @Around("requestInterceptor()")
    public Object interceptorDo(ProceedingJoinPoint point) throws BusinessException {
        try {
            // 1. 获取目标方法信息
            Object target = point.getTarget(); // 目标对象
            Object[] arguments = point.getArgs(); // 方法参数
            String methodName = point.getSignature().getName(); // 方法名
            Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes(); // 参数类型

            // 2. 获取目标方法对象
            Method method = target.getClass().getMethod(methodName, parameterTypes);

            // 3. 获取方法上的@GlobalInterceptor注解
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            if (interceptor == null) {
                return point.proceed(); // 无注解，直接执行原方法
            }

//            // 4. 校验登录（根据注解配置决定是否执行）
//            if (interceptor.checkLogin()) {
//                checkLoginStatus(); // 需补充登录校验逻辑（如检查Session/Token）
//            }

            // 5. 校验参数（根据注解配置决定是否执行）
            if (interceptor.checkParams()) {
                validateParams(method, arguments); // 参数校验核心方法
            }

            // 6. 执行原方法并返回结果
            Object pointResult = point.proceed();
            return pointResult;

        } catch (BusinessException e) {
            log.error("全局拦截器业务异常", e);
            throw e;
        } catch (Exception e) {
            log.error("全局拦截器系统异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        } catch (Throwable e) {
            log.error("全局拦截器未知异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }
    /**
     * 参数校验核心方法（遍历方法参数，校验带@VerifyParam注解的参数）
     */
    private void validateParams(Method method, Object[] arguments) {
        Parameter[] parameters = method.getParameters(); // 获取方法所有参数
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object value = arguments[i]; // 参数值
            VerifyParam verifyParam = parameter.getAnnotation(VerifyParam.class); // 获取@VerifyParam注解

            if (verifyParam == null) {
                continue; // 无注解，跳过校验
            }

            // 判断参数类型是否为基础类型（基础类型直接校验，非基础类型递归校验对象属性）
            String paramType = parameter.getParameterizedType().getTypeName();
            if (ArrayUtils.contains(TYPE_BASE, paramType)) {
                checkValue(value, verifyParam); // 基础类型直接校验
            } else {
                checkObjValue(parameter, value); // 非基础类型（对象）递归校验（需补充实现）
            }
        }
    }


    /**
     * 基础类型参数校验（空值、长度、正则）
     */
    private void checkValue(Object value, VerifyParam verifyParam) {
        // 1. 计算空值和长度
        Boolean isEmpty = value == null || StringUtil.isEmpty(value.toString());
        Integer length = value == null ? 0 : value.toString().length();

        // 2. 校验空值（必填项）
        if (isEmpty && verifyParam.required()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // 3. 校验长度（非空时）
        if (!isEmpty) {
            int min = verifyParam.min();
            int max = verifyParam.max();
            if ((min != -1 && length < min) || (max != -1 && length > max)) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }

        // 4. 校验正则（非空且正则不为空时）
        String regex = verifyParam.verifyRegex().getRegex(); // 从枚举中获取正则
        if (!isEmpty && !StringUtil.isEmpty(regex)) {
            boolean isMatch = VerifyUtils.verify(regex, String.valueOf(value)); // 正则匹配
            if (!isMatch) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }
    }
    /**
     * 对象类型参数校验（递归校验对象属性）
     */
    private void checkObjValue(Parameter parameter, Object value) {
        if (value == null) {
            return;
        }
        Class<?> clazz = value.getClass();
        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            VerifyParam verifyParam = field.getAnnotation(VerifyParam.class);
            if (verifyParam == null) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(value);
                checkValue(fieldValue, verifyParam);
            } catch (IllegalAccessException e) {
                throw new BusinessException(ResponseCodeEnum.CODE_500);
            }
        }
    }


    /**
     * 登录状态校验（需补充具体逻辑，如检查Session/Token）
     */
    private void checkLoginStatus() {
        // TODO: 实现登录校验（如从Session获取用户信息，或解析Token）
        // 示例：HttpSession session = ...; if (session.getAttribute("user") == null) { throw new BusinessException("未登录"); }
    }
}
