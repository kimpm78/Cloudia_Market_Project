package com.cloudia.backend.common.model.pg;

import lombok.Data;

/**
 * PG 결제 취소 요청 DTO
 * - PG로 전달하는 필수 값
 * - 내부 시스템에서 취소 처리에 필요한 값(PK)
 */
@Data
public class PGCancelRequest {

    /** 
     *  PG 필수 구성 요소 (쿠키페이 기준)
     */
    private String tid;        // PG 거래번호 (TID)
    private Integer amount;    // 취소 금액 (부분/전체)
    private String reason;     // 취소 사유
    private String pgType;     // PG 종류 (쿠키페이)

    /**
     *  내부 시스템에서 DB 갱신을 위해 필요한 정보
     */
    private String paymentId;    // 내부 payments PK
    private Long orderId;      // 내부 orders PK
    private String orderNumber; // 내부 주문 번호

    /**
     *  선택 옵션 (일부 PG 에만 사용)
     */
    private Integer taxfreeAmt; // 비과세 취소 금액 (복합과세 시)
    private Integer cancelRemainAmount; // 남은 취소 가능 금액(웰컴페이 전용)
    private String bank;        // 환불 은행 코드 (가상계좌 시)
    private String accountNo;   // 환불 계좌번호 (가상계좌 시)
    private String accountName; // 환불 예금주명 (가상계좌 시)
}
