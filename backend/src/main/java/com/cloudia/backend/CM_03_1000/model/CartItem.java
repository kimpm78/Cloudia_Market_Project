package com.cloudia.backend.CM_03_1000.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * カート項目詳細
 */
@Getter
@Setter
@AllArgsConstructor
public class CartItem {
    private String productId;   // 商品ID
    private String productName; // 商品名
    private int quantity;       // 数量
    private int price;          // 単価または合計金額
}