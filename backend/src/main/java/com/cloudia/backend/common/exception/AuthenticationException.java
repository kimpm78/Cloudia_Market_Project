package com.cloudia.backend.common.exception;

/**
 * 認証例外
 */
public class AuthenticationException extends BaseException {

    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthenticationException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
