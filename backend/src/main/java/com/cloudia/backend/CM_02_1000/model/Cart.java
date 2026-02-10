package com.cloudia.backend.CM_02_1000.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * カートの要約情報を保持するDTO
 */
@Getter
@Setter
public class Cart {
    private int itemCount;         // カートに入っている商品数
    private int totalAmount;       // カートの合計金額
    private List<CartItem> items;  // カートに入っている商品一覧

    public Cart(List<CartItem> items, int itemCount, int totalAmount) {
        this.items = items;
        this.itemCount = itemCount;
        this.totalAmount = totalAmount;
    }
    public Cart(int itemCount, int totalAmount) {
        this(null, itemCount, totalAmount);
    }
}