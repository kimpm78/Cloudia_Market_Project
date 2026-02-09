package com.cloudia.backend.CM_90_1010.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StatusDto {
    private Integer newOrder; // 신규 주문
    private Integer cancelRequested; // 취소 확정
    private Integer reserved; // 예약 확정
    private Integer paymentPending; // 입금 대기
    private Integer answerPending; // 답변 대기
    private Integer answerCompleted; // 답변 완료
    private Integer exchangeInProgress; // 교환 처리중
    private Integer exchangeCompleted; // 교환 완료
    private Integer refundInProgress; // 환불 처리중
    private Integer refundCompleted; // 환불 완료
    private Integer purchaseConfirmed; // 구매 확정
    private Integer preparingShipment; // 배송 준비중
    private Integer inTransit; // 배송중
    private Integer delivered; // 배송 완료
    private Integer exchangeRequested; // 교환 요청
    private Integer refundRequested; // 환불 요청
}
