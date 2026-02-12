package com.cloudia.backend.common.util;

import java.util.Objects;

import com.cloudia.backend.common.model.ResponseModel;

/**
 * ResponseModel生成ヘルパークラス
 */
public class ResponseHelper {
    private ResponseHelper() {
        // インスタンス生成防止
    }

    /**
     * 成功レスポンス生成
     */
    public static <T> ResponseModel<T> success(T data, String message) {
        return ResponseModel.<T>builder()
                .resultList(data)
                .result(true)
                .message(Objects.requireNonNull(message, "メッセージは必須です"))
                .build();
    }

    /**
     * 成功レスポンス生成（デフォルトメッセージ）
     */
    public static <T> ResponseModel<T> success(T data) {
        return success(data, "処理しました");
    }

    /**
     * 失敗レスポンス生成
     */
    public static <T> ResponseModel<T> fail(T data, String message) {
        return ResponseModel.<T>builder()
                .resultList(data)
                .result(false)
                .message(Objects.requireNonNull(message, "メッセージは必須です"))
                .build();
    }

    /**
     * 失敗レスポンス生成（データなし）
     */
    public static <T> ResponseModel<T> fail(String message) {
        return fail(null, message);
    }
}
