package com.cloudia.backend.CM_02_1000.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 장바구니 요약 정보를 담는 DTO 클래스
 * itemCount: 장바구니에 담긴 상품 개수
 * totalAmount: 장바구니 총 금액
 */
@Getter
@Setter
public class Cart {
    private int itemCount; // 장바구니에 담긴 상품 개수
    private int totalAmount; // 장바구니 총 금액
    private List<CartItem> items;

    public Cart(List<CartItem> items, int itemCount, int totalAmount) {
        this.items = items;
        this.itemCount = itemCount;
        this.totalAmount = totalAmount;
    }
    public Cart(int itemCount, int totalAmount) {
        this(null, itemCount, totalAmount);
    }
}