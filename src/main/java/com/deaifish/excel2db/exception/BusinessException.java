package com.deaifish.excel2db.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description 业务异常
 *
 * @author cxx
 * @date 2025-09-30 15:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {
    
    /**
     * 错误码
     */
    private String code;
    
    /**
     * 错误消息
     */
    private String message;

    public BusinessException(String message) {
        super(message);
        this.code = "500";
        this.message = message;
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}

