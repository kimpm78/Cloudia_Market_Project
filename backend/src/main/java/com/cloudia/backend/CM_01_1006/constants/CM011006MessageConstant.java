package com.cloudia.backend.CM_01_1006.constants;

public final class CM011006MessageConstant {

    private CM011006MessageConstant() {
    }

    public static final String LOG_INQUIRY_LIST_START = "1:1 문의 목록 조회 요청. loginId: {}";
    public static final String LOG_PRODUCT_LIST_ERROR = "상품 목록 조회 실패";
    public static final String LOG_INQUIRY_LIST_ERROR = "1:1 문의 목록 조회 중 오류 발생. loginId: {}";
    public static final String LOG_INQUIRY_CREATE_START = "1:1 문의 등록 요청. loginId: {}";
    public static final String LOG_INQUIRY_CREATE_ERROR = "1:1 문의 등록 중 오류 발생. loginId: {}";
    public static final String LOG_VALIDATION_FAIL = "문의 등록 유효성 검사 실패: {}";
    public static final String LOG_INQUIRY_DETAIL_ERROR = "1:1 문의 상세 조회 중 오류 발생. inquiryId: {}";
    public static final String LOG_INQUIRY_DETAIL_FORBIDDEN = "비공개 문의 접근 차단. inquiryId: {}, requesterId: {}";
    public static final String LOG_ANSWER_REG_ERROR = "답변 등록 중 오류 발생. inquiryId: {}";
    public static final String LOG_ANSWER_STATUS_FAIL = "답변 등록 후 상태 변경 실패. inquiryId: {}";
    public static final String LOG_ANSWER_VALIDATION_FAIL = "답변 등록 유효성 검사 실패: {}";
    public static final String LOG_DELETE_START = "1:1 문의 삭제 요청. inquiryId: {}, requesterId: {}";
    public static final String LOG_DELETE_FORBIDDEN = "문의 삭제 권한 없음 (작성자 불일치). inquiryId: {}, requesterId: {}";
    public static final String LOG_DELETE_FAIL_ANSWERED = "문의 삭제 불가 (답변 완료됨). inquiryId: {}, status: {}";
    public static final String LOG_DELETE_ERROR = "1:1 문의 삭제 중 DB 오류 발생. inquiryId: {}";
    public static final String LOG_DELETE_UNKNOWN_ERROR = "문의 삭제 중 알 수 없는 오류";
    public static final String MSG_SERVICE_ERROR = "서비스 오류";
    public static final String MSG_VALIDATION_ERROR = "입력값이 올바르지 않습니다.";
    public static final String MSG_CREATE_SUCCESS = "문의가 성공적으로 등록되었습니다.";
    public static final String MSG_CREATE_ERROR = "문의 등록 중 오류가 발생했습니다.";
    public static final String MSG_USER_NOT_FOUND = "사용자 정보를 찾을 수 없습니다.";
    public static final String MSG_ANSWER_EMPTY = "답변 내용을 입력해주세요.";
    public static final String MSG_ANSWER_ERROR = "답변 등록 중 오류가 발생했습니다.";
    public static final String MSG_DELETE_SUCCESS = "문의가 삭제되었습니다.";
    public static final String MSG_DELETE_FAIL_ANSWERED = "답변이 완료된 문의는 삭제할 수 없습니다.";
    public static final String MSG_DELETE_ERROR = "문의 삭제 중 알 수 없는 오류가 발생했습니다.";
    public static final String MSG_NOT_FOUND = "해당 문의를 찾을 수 없습니다.";
}