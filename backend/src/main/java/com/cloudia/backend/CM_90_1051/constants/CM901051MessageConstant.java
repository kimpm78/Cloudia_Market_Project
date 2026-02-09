package com.cloudia.backend.CM_90_1051.constants;

public class CM901051MessageConstant {
    // 조회 관련 메시지
    public static final String ORDER_FIND_ALL_START = "주문 전체 조회를 시작합니다.";
    public static final String ORDER_FIND_ALL_COMPLETE = "주문 전체 조회가 완료되었습니다. 조회된 건수: {} 건";
    public static final String ORDER_FIND_ALL_DB_ERROR = "주문 전체 조회 중 데이터베이스 오류가 발생했습니다. 오류: {}";
    public static final String ORDER_FIND_ALL_NULL_ERROR = "주문 전체 조회 중 NULL 오류가 발생했습니다. 오류: {}";
    public static final String ORDER_FIND_ALL_UNEXPECTED_ERROR = "주문 전체 조회 중 예상치 못한 오류가 발생했습니다. 오류: {}";

    public static final String ORDER_SEARCH_START = "주문 검색을 시작합니다.";
    public static final String ORDER_SEARCH_COMPLETE = "주문 검색이 완료되었습니다. 조회된 건수: {} 건";
    public static final String ORDER_SEARCH_DB_ERROR = "주문 검색 중 데이터베이스 오류가 발생했습니다. 오류: {}";
    public static final String ORDER_SEARCH_NULL_ERROR = "주문 검색 중 NULL 오류가 발생했습니다. 오류: {}";
    public static final String ORDER_SEARCH_UNEXPECTED_ERROR = "주문 검색 중 예상치 못한 오류가 발생했습니다. 오류: {}";

    public static final String ORDER_DETAIL_FIND_START = "주문 상세 조회를 시작합니다.";
    public static final String ORDER_DETAIL_FIND_COMPLETE = "주문 상세 조회가 완료되었습니다. 조회된 건수: {} 건";
    public static final String ORDER_DETAIL_FIND_DB_ERROR = "주문 상세 조회 중 데이터베이스 오류가 발생했습니다. 오류: {}";
    public static final String ORDER_DETAIL_FIND_NULL_ERROR = "주문 상세 조회 중 NULL 오류가 발생했습니다. 오류: {}";
    public static final String ORDER_DETAIL_FIND_UNEXPECTED_ERROR = "주문 상세 조회 중 예상치 못한 오류가 발생했습니다. 오류: {}";

    // 업데이트 관련 메시지
    public static final String ORDER_UPDATE_START = "업데이트를 시작합니다.";
    public static final String ORDER_UPDATE_EMPTY_REQUEST = "업데이트 요청 데이터가 비어있습니다.";
    public static final String ORDER_UPDATE_NOT_FOUND = "업데이트할 주문을 찾을 수 없습니다.";
    public static final String ORDER_UPDATE_DUPLICATE_ERROR = "주문 업데이트 중 중복 키 오류가 발생했습니다. 오류: {}";
    public static final String ORDER_UPDATE_INTEGRITY_ERROR = "주문 업데이트 중 데이터 무결성 위반이 발생했습니다. 오류: {}";
    public static final String ORDER_UPDATE_DB_ERROR = "주문 업데이트 중 데이터베이스 오류가 발생했습니다. 오류: {}";
    public static final String ORDER_UPDATE_UNEXPECTED_ERROR = "주문 업데이트 중 예상치 못한 오류가 발생했습니다. 오류: {}";

    // 성공 메시지
    public static final String SUCCESS_ORDER_FIND = "주문 조회가 성공적으로 완료되었습니다.";
    public static final String SUCCESS_ORDER_SEARCH = "주문 검색이 성공적으로 완료되었습니다.";
    public static final String SUCCESS_ORDER_DETAIL_FIND = "주문 상세 조회가 성공적으로 완료되었습니다.";
    public static final String SUCCESS_ORDER_UPDATE = "주문 상태가 성공적으로 업데이트되었습니다.";

    // 이메일 발송 관련 메시지
    public static final String EMAIL_START = "이메일 발송을 시작합니다.";
    public static final String EMAIL_VALIDATION = "이메일 검증을 시작합니다.";
    public static final String EMAIL_CUSTOMER_INFO_NOT_FOUND = "고객 이메일 정보가 없어 배송 알림을 발송할 수 없습니다.";
    public static final String EMAIL_ORDER_INFO_NOT_FOUND = "주문 정보를 찾을 수 없어 배송 알림을 발송할 수 없습니다.";
    public static final String EMAIL_SEND_SUCCESS = "배송 시작 이메일 발송 성공 - 주문번호: {}";
    public static final String EMAIL_SEND_INPUT_ERROR = "이메일 발송 입력값 오류 - 주문번호: {}, 에러: {}";
    public static final String EMAIL_SEND_SYSTEM_ERROR = "이메일 발송 시스템 오류 - 주문번호: {}, 에러: {}";
    public static final String EMAIL_SEND_GENERAL_ERROR = "배송 시작 이메일 발송 실패 - 주문번호: {}, 에러: {}";
    public static final String EMAIL_SEND_FAILED_INPUT = "배송 알림 발송 실패: {}";
    public static final String EMAIL_SEND_FAILED_SYSTEM = "배송 알림 발송 실패: 시스템 오류가 발생했습니다.";
    public static final String EMAIL_SEND_FAILED_GENERAL = "배송 알림 이메일 발송에 실패했습니다.";
    public static final String EMAIL_ORDER_INFO_QUERY_ERROR = "고객 이메일 정보 없음 - 주문번호: {}";
    public static final String EMAIL_ORDER_QUERY_FAILED = "주문 정보 조회 실패 - 주문번호: {}";
}
