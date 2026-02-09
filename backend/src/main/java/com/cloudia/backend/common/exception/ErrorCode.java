package com.cloudia.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * エラーコードを定義
 */
@Getter
public enum ErrorCode {
    /* 共通 */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "1000", "サーバー内部エラーが発生しました。"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "1002", "許可されていないHTTPメソッドです。"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "1005", "入力タイプが正しくありません。"),

    /* Validation */
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "1100", "入力値が正しくありません。"),
    VALIDATION_SEARCH_TERM_EMPTY(HttpStatus.BAD_REQUEST, "1101", "検索キーワードを入力してください。"),
    VALIDATION_FIELD_REQUIRED(HttpStatus.BAD_REQUEST, "1102", "必須入力項目が未入力です。"),

    /* 認証・認可 */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "1401", "無効なトークンです。"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "1403", "アクセス権限がありません。"),

    /* ビジネス */
    UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "1200", "更新に失敗しました。"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "1201", "要求されたリソースが見つかりません。"),

    /* DB */
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "2100", "データベースエラーが発生しました。"),

    /* 商品在庫登録エラーメッセージ */
    MSG_PRODUCT_REGISTERED(HttpStatus.NOT_FOUND, "1201", "商品はすでに登録されています。"),
    MSG_STOCK_EXISTS(HttpStatus.INTERNAL_SERVER_ERROR, "901065", "在庫が既に存在します。");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
