package com.cloudia.backend.common.log;

import lombok.Getter;

@Getter
public enum LogMessage {
    // 共通参照
    COMMON_SELECT_START("10001"),
    COMMON_SELECT_SUCCESS("10002"),
    COMMON_SELECT_FAIL("10003"),
    COMMON_SELECT_EMPTY("10004"),

    // 共通作成
    COMMON_INSERT_START("10101"),
    COMMON_INSERT_SUCCESS("10102"),
    COMMON_INSERT_FAIL("10103"),

    // 共通更新
    COMMON_UPDATE_START("10201"),
    COMMON_UPDATE_SUCCESS("10202"),
    COMMON_UPDATE_FAIL("10203"),
    COMMON_UPDATE_EMPTY("10204"),

    // 共通削除
    COMMON_DELETE_START("10301"),
    COMMON_DELETE_SUCCESS("10302"),
    COMMON_DELETE_FAIL("10303"),

    // 認証
    AUTH_TOKEN_INVALID("10401"),

    // データベース
    COMMON_UNEXPECTED_ERROR("90000"),
    COMMON_BUSINESS_EXCEPTION("90001"),
    VALIDATION_INPUT_INVALID("90002"),
    VALIDATION_TYPE_MISMATCH("90003"),
    HTTP_METHOD_NOT_SUPPORTED("90004"),
    DB_ACCESS_ERROR("90005"),
    COMMON_NULL_POINTER_ERROR("90006");

    private final String code;

    LogMessage(String code) {
        this.code = code;
    }

    public String getMessage(Object... args) {
        return LogMessageLoader.getMessage(this.code, args);
    }

    public LogCategory getCategory() {
        return LogMessageLoader.getCategory(this.code);
    }
}