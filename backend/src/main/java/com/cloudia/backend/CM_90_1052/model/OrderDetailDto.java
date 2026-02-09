package com.cloudia.backend.CM_90_1052.model;

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
public class OrderDetailDto {
    private String productId; // 주문 상세 ID
    private String productName; // 상품명
    private String quantity; // 수량
    private String unitPrice; // 개당 금액
}
