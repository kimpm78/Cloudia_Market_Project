package com.cloudia.backend.CM_03_1000.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 장바구니 항목 상세
 */
@Getter
@Setter
@AllArgsConstructor
public class CartItem {
    private String productId;   // 상품 ID
    private String productName; // 상품 이름
    private int quantity;       // 수량
    private int price;          // 개당 가격 또는 총합
}
