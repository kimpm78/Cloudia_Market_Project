package com.cloudia.backend.CM_90_1051.constants;

public class CM901051Constant {
    public static final int ORDER_STATUS_ALL = 0; // 전체
    public static final int ORDER_STATUS_REMITTANCE_PENDING = 1; // 송금확인중
    public static final int ORDER_STATUS_CONFIRMED = 2; // 구매 확정
    public static final int ORDER_STATUS_PREPARING_SHIPMENT = 3; // 배송 준비중
    public static final int ORDER_STATUS_SHIPPING = 4; // 배송중
    public static final int ORDER_STATUS_DELIVERED = 5; // 배송 완료
    public static final int ORDER_STATUS_CANCELED = 6; // 구매 취소

    public static final int PAYMENT_METHOD_BANK_TRANSFER = 1; // 계좌이체
    public static final int PAYMENT_METHOD_CREDIT_CARD = 2; // 신용카드
    public static final String PAYMENT_METHOD_BANK_TRANSFER_STRING = "계좌이체"; // 계좌이체
    public static final String PAYMENT_METHOD_CREDIT_CARD_STRING = "신용카드"; // 신용카드
}
