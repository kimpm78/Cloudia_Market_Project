package com.cloudia.backend.CM_90_1051.model;

import java.time.LocalDateTime;

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
public class OrderDto {
    private Integer orderId; // 주문 ID
    private String memberNumber; // 사원 번호
    private String loginId; // 아이디
    private String email; // 이메일
    private String name; // 성명
    private String orderNumber; // 주문 번호
    private Double totalAmount; // 주문 총액
    private Integer paymentValue; // 결제 수단
    private Integer orderStatusValue; // 상태
    private String shippingCompany; // 배송 업체
    private String trackingNumber; // 배송 조회 번호
    private String shippingDate; // 실제 배송일
    private String deliveryDate; // 배송 예정일
    private LocalDateTime orderDate; // 주문 일시
    private String updatedBy; // 업데이트자
    private LocalDateTime updatedAt; // 업데이트일
}
