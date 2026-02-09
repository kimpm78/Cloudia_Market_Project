package com.cloudia.backend.CM_02_1000.constants;

public class CM021000MessageConstant {
    private CM021000MessageConstant() {
    }

    // 헤더 메뉴 조회 관련
    public static final String HEADER_MENU_START = "헤더 메뉴 정보 조회 시작";
    public static final String HEADER_MENU_END = "헤더 메뉴 정보 조회 완료";
    public static final String HEADER_ICON_START = "헤더 메뉴 정보 조회 시작";
    public static final String HEADER_ICON_END = "헤더 메뉴 정보 조회 시작";
    public static final String HEADER_MENU_COUNT = "헤더 메뉴 수: {}";
    public static final String HEADER_MENU_DB_ERROR = "DB 접근 중 오류 발생 (헤더 메뉴): {}";
    public static final String HEADER_MENU_NULL_ERROR = "NullPointerException 발생 (헤더 메뉴): {}";
    public static final String HEADER_MENU_UNKNOWN_ERROR = "예상치 못한 오류 발생 (헤더 메뉴): {}";
    
    // 헤더 아이콘 조회 관련
    public static final String HEADER_ICON_COUNT = "아이콘 메뉴 수: {}";
    public static final String HEADER_ICON_DB_ERROR = "DB 접근 중 오류 발생 (아이콘 메뉴): {}";
    public static final String HEADER_ICON_NULL_ERROR = "NullPointerException 발생 (아이콘 메뉴): {}";
    public static final String HEADER_ICON_UNKNOWN_ERROR = "예상치 못한 오류 발생 (아이콘 메뉴): {}";

    // 장바구니 관련
    public static final String CART_ADD_START = "장바구니 추가 처리 시작";
    public static final String CART_ADD_SUCCESS = "장바구니 추가 완료";
    public static final String CART_ADD_FAILED = "장바구니 추가 실패";
    public static final String CART_ADD_RESPONSE = "장바구니에 상품이 추가되었습니다.";
    public static final String CART_LOGIN_REQUIRED = "로그인이 필요합니다.";
    public static final String CART_GET_SUCCESS = "장바구니 조회 완료: {}건, 총액 {}원";

    // 배너 조회 관련
    public static final String BANNER_FIND_ALL_START = "배너 전체 리스트 조회 시작";
    public static final String BANNER_FIND_ALL_COMPLETE = "배너 전체 리스트 조회 완료, 조회된 배너 수: {}";
    public static final String BANNER_FIND_ALL_DB_ERROR = "배너 전체 리스트 조회 중 DB 오류 발생: {}";
    public static final String BANNER_FIND_ALL_UNEXPECTED_ERROR = "배너 전체 리스트 조회 중 예상치 못한 오류 발생: {}";

    public static final String BANNER_SEARCH_START = "배너 검색 시작, 검색어: {}";
    public static final String BANNER_SEARCH_COMPLETE = "배너 검색 완료, 조회된 배너 수: {}";
    public static final String BANNER_SEARCH_FAILED_EMPTY_TERM = "배너 검색 실패: 검색어가 비어있습니다.";
    public static final String BANNER_SEARCH_DB_ERROR = "배너 검색 중 DB 오류 발생, 검색어: {}, 오류: {}";
    public static final String BANNER_SEARCH_UNEXPECTED_ERROR = "배너 검색 중 예상치 못한 오류 발생, 검색어: {}, 오류: {}";

    public static final String BANNER_FIND_BY_ID_START = "배너 상세 조회 시작, 배너 ID: {}";
    public static final String BANNER_FIND_BY_ID_COMPLETE = "배너 상세 조회 완료, 배너 ID: {}";
    public static final String BANNER_FIND_BY_ID_FAILED_INVALID_ID = "배너 상세 조회 실패: 유효하지 않은 배너 ID입니다. 배너 ID: {}";
    public static final String BANNER_FIND_BY_ID_DB_ERROR = "배너 상세 조회 중 DB 오류 발생, 배너 ID: {}, 오류: {}";
    public static final String BANNER_FIND_BY_ID_UNEXPECTED_ERROR = "배너 상세 조회 중 예상치 못한 오류 발생, 배너 ID: {}, 오류: {}";

    // ========================================
    // 응답 메시지
    // ========================================

    // 성공 메시지
    public static final String SUCCESS_BANNER_FIND = "배너 조회 성공";

    // 실패 메시지
    
}