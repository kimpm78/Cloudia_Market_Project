package com.cloudia.backend.CM_90_1052.model;

import java.time.LocalDateTime;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
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
public class ReturnsDto {
    private int returnId; // 리턴 ID
    private String orderNo; // 요청 번호
    private String customerId; // 사원 번호
    private LocalDateTime completedAt; // 교환/환불 완료일
    private String reason; // 메모
    private int totalAmount; // 총 환불 금액
    private int shippingFeeCustomerAmount; // 배달비(고객부담)
    private int shippingFeeSellerAmount; // 배달비(매장부담)
    private int refundAmount; // 환불 금액
    private int returnStatusValue; // 상태
    private String requestedAt; // 환불/교환 요청일
    private String updatedBy; // 수정자
    private LocalDateTime updatedAt; // 수정일
}
