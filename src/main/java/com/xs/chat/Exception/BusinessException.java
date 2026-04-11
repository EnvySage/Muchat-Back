package com.xs.chat.Exception;
import com.xs.chat.enumeration.ResponseCodeEnum;
import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int code;  // 错误码

    public BusinessException(String message) {
        super(message);
        this.code = 0;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResponseCodeEnum responseCode) {
        super(responseCode.getMsg());
        this.code = responseCode.getCode();
    }

    public BusinessException(ResponseCodeEnum responseCode, String customMsg) {
        super(customMsg != null ? customMsg : responseCode.getMsg());
        this.code = responseCode.getCode();
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 0;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
