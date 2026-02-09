package com.cloudia.backend.CM_90_1050.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ResultDto {
    private String memberNumber; // 사원 번호
    private String loginId; // 로그인ID
    private String productName; // 상품명
    private Integer totalAmount; // 정산 금액
    private Integer unitPrice; // 개당 금액
    private Integer shippingCost; // 배송비
    private Integer discountAmount; // 할인금액
    private Integer price; // 사입가
    private LocalDateTime orderDate; // 주문일
    private Integer quantity; // 구매 수량
}
