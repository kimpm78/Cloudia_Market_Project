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
public class PreviousInfoDto {
    private Integer previousNetSales;       // 어제의 순매출 현황
    private Integer settlementAmount;       // 매출 금액
    private Integer refundAmount;           // 환불 금액
    private Integer orderCount;             // 주문 건수
    private Integer refundCount;            // 환불/반품 건수
    private Integer shipmentProcessedCount; // 배송 처리 건수
    private Integer deliveredCount;         // 배송 완료 건수
}
