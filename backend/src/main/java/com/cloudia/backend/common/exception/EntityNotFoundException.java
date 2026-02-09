package com.cloudia.backend.common.exception;

/**
 * エンティティが見つからない場合に発生する例外
 */
public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EntityNotFoundException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
