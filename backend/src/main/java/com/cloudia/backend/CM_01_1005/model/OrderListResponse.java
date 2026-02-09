package com.cloudia.backend.CM_01_1005.model;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListResponse {
    private String orderNo; // 주문 번호
    private String orderDate; // 주문 일자
    private String productName; // 상품명
    private String deliveryDate; // 발송 예정일
    private Long totalPrice; // 가격/결제수단
    private int paymentValue;
    private String orderStatus; // 상태
    private int orderStatusValue; // 상태 값
    private LocalDateTime paymentAt; // 72시간 계산의 기준이 될 시각
    private String productImageUrl;
}