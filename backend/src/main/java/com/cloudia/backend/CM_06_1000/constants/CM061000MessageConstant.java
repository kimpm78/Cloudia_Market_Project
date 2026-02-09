package com.cloudia.backend.CM_06_1000.constants;

public class CM061000MessageConstant {
    private CM061000MessageConstant() {}

    // 조회
    public static final String CART_FETCH_SUCCESS = "장바구니 조회 성공";
    public static final String CART_FETCH_EMPTY   = "장바구니에 담긴 상품이 없습니다.";

    // 담기
    public static final String CART_ADD_SUCCESS = "상품이 장바구니에 담겼습니다.";
    public static final String CART_ADD_FAIL    = "장바구니 담기 중 오류가 발생했습니다.";

    // 수량 변경
    public static final String CART_UPDATE_QTY_SUCCESS = "장바구니 수량이 변경되었습니다.";
    public static final String CART_UPDATE_QTY_FAIL    = "장바구니 수량 변경 중 오류가 발생했습니다.";

    // 삭제
    public static final String CART_DELETE_SUCCESS = "장바구니 상품이 삭제되었습니다.";
    public static final String CART_DELETE_FAIL    = "장바구니 상품 삭제 중 오류가 발생했습니다.";

    // 재고 및 유효성
    public static final String CART_OUT_OF_STOCK      = "수량이 없습니다.";
    public static final String CART_ITEM_NOT_FOUND    = "장바구니 정보를 찾을 수 없습니다.";
    public static final String CART_INVALID_QUANTITY  = "수량은 1개 이상이어야 합니다.";
    public static final String CART_LIMIT_EXCEEDED    = "장바구니에는 최대 10개까지만 담을 수 있습니다.";
    public static final String CART_PRODUCT_LIMIT_EXCEEDED = "동일 상품은 최대 %d개까지만 담을 수 있습니다.";
    public static final String CART_RESERVATION_MONTH_MISMATCH_ON_ADD =
            "출시월이 다른 예약상품과 일반 상품은 함께 담을 수 없습니다.";
    public static final String CART_RESERVATION_MONTH_UNKNOWN_ON_ADD =
            "출시월 정보를 확인할 수 없어 예약상품과 일반 상품을 함께 담을 수 없습니다.";
    public static final String CART_RESERVATION_ONLY_MONTH_MISMATCH_ON_ADD =
            "출시월이 다른 예약상품은 함께 담을 수 없습니다.";

    // 주문 준비(선택 항목)
    public static final String CART_PREPARE_ORDER_SUCCESS = "선택한 상품으로 주문을 준비했습니다.";
    public static final String CART_PREPARE_ORDER_EMPTY   = "선택된 장바구니 상품이 없습니다.";
    public static final String CART_PREPARE_ORDER_FAIL    = "주문 준비 중 오류가 발생했습니다.";
    public static final String CART_MIXED_ORDER_NOT_ALLOWED = "통상 상품과 예약 상품은 함께 결제할 수 없습니다.";
    public static final String CART_RESERVATION_MONTH_MISMATCH = "예약 상품은 동일한 출시월 상품끼리만 함께 결제할 수 있습니다.";
    public static final String CART_RESERVATION_MONTH_UNKNOWN = "예약 상품의 출시월 정보를 확인할 수 없습니다.";

    // 유효성 + 공통 에러 로그
    public static final String CART_VALIDATION_FAIL   = "장바구니 유효성 검사 실패: {}";
    public static final String CART_DB_ERROR          = "장바구니 처리 중 DB 오류 발생: {}";
}
