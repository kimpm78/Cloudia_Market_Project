package com.cloudia.backend.CM_04_1000.constants;

public class CM041000MessageConstant {
    private CM041000MessageConstant() {
    }
    // ========================================
    // 리뷰/후기 관련
    // ========================================

    // 등록/수정/삭제
    public static final String REVIEW_CREATE_SUCCESS = "리뷰가 성공적으로 생성되었습니다.";
    public static final String REVIEW_REGISTER_SUCCESS = "리뷰가 성공적으로 등록되었습니다.";
    public static final String REVIEW_REGISTER_FAIL = "리뷰 등록 중 오류가 발생했습니다.";
    public static final String REVIEW_WRITE_SUCCESS = "리뷰가 성공적으로 작성되었습니다.";
    public static final String REVIEW_WRITE_FAIL = "리뷰 작성 중 오류가 발생했습니다.";
    public static final String REVIEW_UPDATE_SUCCESS = "리뷰가 성공적으로 수정되었습니다.";
    public static final String REVIEW_UPDATE_FAIL = "리뷰 수정 중 오류가 발생했습니다.";
    public static final String REVIEW_DELETE_SUCCESS = "리뷰가 성공적으로 삭제되었습니다.";
    public static final String REVIEW_DELETE_FAIL = "리뷰 삭제 중 오류가 발생했습니다.";

    // ========================================
    // 리뷰/후기 조회수 관련
    // ========================================
    public static final String REVIEW_FETCH_SUCCESS = "리뷰 목록 조회 성공";
    public static final String REVIEW_DETAIL_FETCH_SUCCESS = "리뷰 상세 조회 성공";
    public static final String REVIEW_DETAIL_FETCH_FAIL = "리뷰 상세 조회 중 오류가 발생했습니다.";
    public static final String REVIEW_NOT_FOUND = "리뷰를 찾을 수 없습니다.";
    public static final String REVIEW_FETCH_EMPTY = "해당 상품에 대한 리뷰가 없습니다.";
    public static final String REVIEW_FETCH_FAIL = "리뷰 조회 중 오류가 발생했습니다.";
    public static final String REVIEW_VIEW_ALREADY_COUNTED = "24시간 내에 이미 조회된 리뷰입니다. 조회수 증가를 생략합니다.";

    // 유효성/권한
    public static final String REVIEW_VALIDATION_FAIL = "리뷰 유효성 검사 실패: {}";
    public static final String REVIEW_FORBIDDEN = "리뷰에 대한 권한이 없습니다.";
    public static final String REVIEW_VALIDATION_ERROR_MSG = "잘못된 요청입니다. 필수값을 확인하세요.";

    // 예외
    public static final String REVIEW_DB_ERROR = "리뷰 처리 중 DB 오류 발생: {}";
    public static final String REVIEW_UNEXPECTED_ERROR = "리뷰 처리 중 예상치 못한 오류 발생: {}";

    // ========================================
    // 리뷰/후기 주문 목록 관련 (작성자용)
    // ========================================

    public static final String REVIEW_ORDER_FETCH_SUCCESS = "주문 목록 조회 성공";
    public static final String REVIEW_ORDER_FETCH_FAIL = "주문 목록 조회 실패";
    public static final String REVIEW_ORDER_GROUP_FETCH_SUCCESS = "주문 목록 조회 성공: memberNumber={}";
    public static final String REVIEW_ORDER_GROUP_FETCH_FAIL = "주문 목록 조회 실패: memberNumber={}, error={}";

    public static final String REVIEW_ORDER_PRODUCT_FOUND = "주문 내 상품 존재 확인 성공";
    public static final String REVIEW_ORDER_PRODUCT_NOT_FOUND = "주문 내 상품을 찾을 수 없음";
    public static final String REVIEW_ORDER_PRODUCT_ERROR = "주문 내 상품 검증 중 오류 발생";

    // ========================================
    // 리뷰/후기 조회수 관련
    // ========================================
    public static final String REVIEW_VIEW_INCREMENT_SUCCESS = "리뷰 조회수 증가 성공";
    public static final String REVIEW_VIEW_INCREMENT_FAIL = "리뷰 조회수 증가 실패";
    public static final String REVIEW_VIEW_INCREMENT_ERROR = "리뷰 조회수 증가 중 오류 발생";
    public static final String REVIEW_VIEW_INCREMENT_ALREADY_COUNTED = "오늘 이미 조회수가 반영되었습니다.";

    // ========================================
    // 리뷰/후기 이미지 관련
    // ========================================
    public static final String REVIEW_IMAGE_UPLOAD_SUCCESS = "리뷰 이미지 업로드 성공";
    public static final String REVIEW_IMAGE_UPLOAD_FAIL = "리뷰 이미지 업로드 실패";
    public static final String REVIEW_IMAGE_DELETE_SUCCESS = "리뷰 이미지 삭제 성공";
    public static final String REVIEW_IMAGE_DELETE_FAIL = "리뷰 이미지 삭제 실패";
    public static final String REVIEW_IMAGE_NOT_FOUND = "리뷰 이미지를 찾을 수 없습니다.";
}
