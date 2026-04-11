package com.xs.chat.Exception.Handler;
import com.xs.chat.Exception.BusinessException;
import com.xs.chat.enumeration.ResponseCodeEnum;
import com.xs.chat.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常：code={}, msg={}", e.getCode(), e.getMessage());

        // 根据错误码返回对应的响应
        if (e.getCode() == ResponseCodeEnum.CODE_500.getCode()) {
            return Result.serverError(e.getMessage());
        } else if (e.getCode() == ResponseCodeEnum.CODE_401.getCode()) {
            return Result.unauthorized(e.getMessage());
        } else if (e.getCode() == ResponseCodeEnum.CODE_403.getCode()) {
            return Result.forbidden(e.getMessage());
        } else if (e.getCode() == ResponseCodeEnum.CODE_600.getCode()) {
            return Result.error(e.getCode(), e.getMessage());
        } else {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数错误：{}", e.getMessage());
        return Result.error(ResponseCodeEnum.CODE_600.getCode(), "参数错误：" + e.getMessage());
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("资源不存在：{}", e.getRequestURL());
        return Result.notFound("请求的资源不存在");
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.serverError("服务器内部错误，请稍后重试");
    }
}
