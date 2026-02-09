package com.cloudia.backend.common.exception;

/**
 * 不正なリクエスト例外
 */
public class InvalidRequestException extends BusinessException {
    public InvalidRequestException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidRequestException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
