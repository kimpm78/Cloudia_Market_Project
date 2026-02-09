package com.cloudia.backend.common.util;

import java.util.Objects;

import com.cloudia.backend.common.model.ResponseModel;

/**
 * ResponseModel 생성 헬퍼 클래스
 */
public class ResponseHelper {
    private ResponseHelper() {
        // 인스턴스 생성 방지
    }

    /**
     * 성공 응답 생성
     */
    public static <T> ResponseModel<T> success(T data, String message) {
        return ResponseModel.<T>builder()
                .resultList(data)
                .result(true)
                .message(Objects.requireNonNull(message, "메시지는 필수입니다"))
                .build();
    }

    /**
     * 성공 응답 생성 (기본 메시지)
     */
    public static <T> ResponseModel<T> success(T data) {
        return success(data, "처리되었습니다");
    }

    /**
     * 실패 응답 생성
     */
    public static <T> ResponseModel<T> fail(T data, String message) {
        return ResponseModel.<T>builder()
                .resultList(data)
                .result(false)
                .message(Objects.requireNonNull(message, "메시지는 필수입니다"))
                .build();
    }

    /**
     * 실패 응답 생성 (데이터 없음)
     */
    public static <T> ResponseModel<T> fail(String message) {
        return fail(null, message);
    }
}
