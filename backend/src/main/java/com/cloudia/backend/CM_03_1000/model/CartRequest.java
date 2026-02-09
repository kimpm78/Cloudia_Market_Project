package com.cloudia.backend.CM_03_1000.model;

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
public class CartRequest {
    private Long userId; // 사용자 ID
    private String productId; // 상품 ID
    private Integer quantity; //장바구니에 담을 수량
    private boolean reservation; // true = 예약상품
public boolean isReservation() { return reservation; }
}
