package com.xs.chat.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一API响应结果封装
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    // 状态码常量
    public static final int SUCCESS = 1;
    public static final int ERROR = 0;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int SERVER_ERROR = 500;

    private int code;    // 状态码
    private String msg;  // 消息
    private T data;     // 数据
    private Long timestamp = System.currentTimeMillis(); // 时间戳

    // 成功（无数据）
    public static <T> Result<T> success() {
        return success(null);
    }

    // 成功（带数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = SUCCESS;
        result.msg = "操作成功";
        result.data = data;
        return result;
    }

    // 成功（自定义消息）
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.code = SUCCESS;
        result.msg = msg;
        result.data = data;
        return result;
    }

    // 错误（默认消息）
    public static <T> Result<T> error() {
        return error("操作失败");
    }

    // 错误（自定义消息）
    public static <T> Result<T> error(String msg) {
        return error(ERROR, msg);
    }

    // 错误（自定义状态码和消息）
    public static <T> Result<T> error(int code, String msg) {
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = msg;
        return result;
    }

    // 未授权
    public static <T> Result<T> unauthorized(String msg) {
        return error(UNAUTHORIZED, msg != null ? msg : "未授权，请登录");
    }

    // 禁止访问
    public static <T> Result<T> forbidden(String msg) {
        return error(FORBIDDEN, msg != null ? msg : "无访问权限");
    }

    // 资源不存在
    public static <T> Result<T> notFound(String msg) {
        return error(NOT_FOUND, msg != null ? msg : "资源不存在");
    }

    // 服务器错误
    public static <T> Result<T> serverError(String msg) {
        return error(SERVER_ERROR, msg != null ? msg : "服务器内部错误");
    }

    // 判断是否成功
    public boolean isSuccess() {
        return code == SUCCESS;
    }
}