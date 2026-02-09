package com.cloudia.backend.common.model.pg;

import lombok.Data;

/**
 * PG 결제 요청(Ready) DTO
 * - 내부 주문 정보
 * - PG 전달 정보
 * - PG별 선택 옵션(다중 PG 대응)
 */
@Data
public class PGReadyRequest {

    /**
     *  내부 시스템(가맹점) 주문 정보
     */
    private Long orderId;          // 내부 주문 PK
    private String orderNumber;    // 내부 주문번호 (가맹점 주문번호)
    private String orderNo;        // PG로 전달되는 주문번호 (보통 orderNumber 와 동일)

    /**
     *  PG 필수 요청 정보
     */
    private String productName;    // 상품명 (쿠키페이: 일부 특수문자 제한)
    private Integer amount;        // 결제 금액
    private String buyerName;      // 결제자 이름
    private String buyerEmail;     // 결제자 이메일 (선택이지만 대부분 사용)
    private String returnUrl;      // PG 결제 결과 callback 주소
    private String homeUrl;        // 결제 완료 후 이동 주소
    private String cancelUrl;      // 결제 중 취소 시 이동 주소
    private String failUrl;        // PG 결제 실패 시 이동 주소
    private String pgType;         // PG 종류 ("COOKIEPAY", "TOSS", "INICIS" 등)

    /**
     *  PG 선택 요청 정보 (PG 정책에 따라 필요 시 사용)
     */
    private Integer taxFreeAmount;    // 비과세 금액 (복합과세 전용)
    private String directResultFlag;  // Firefox cross-domain issue 대응 (키움페이)
    private String mtype;             // WebView 결제 구분 (키움페이/원글로벌페이)
    private String payMethod;         // 결제 수단 (CARD/BANK)
    private String quota;             // 카드 할부기간 (00=일시불)
    private String buyerId;           // 내부 고객 ID
    private String taxYn;             // 과세/비과세 구분 (Y/N/M)
    private String closeUrl;          // 취소 후 이동 URL (카카오페이)
    private String escrow;            // 에스크로 결제 여부
    private String engFlag;           // 해외 영문 결제창 출력 여부 (키움페이)
    private String payType;           // 해외결제/특수카드 타입 (쿠키페이 PAY_TYPE)
    private String cardList;          // 해외 카드 종류 선택
    private String encYn;             // 암호화 리턴 여부 (Y=암호화 데이터 사용)
}
