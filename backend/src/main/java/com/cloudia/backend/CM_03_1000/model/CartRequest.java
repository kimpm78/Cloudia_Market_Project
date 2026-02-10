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
    private Long userId;         // ユーザーID
    private String productId;    // 商品ID
    private Integer quantity;    // カートに追加する数量
    private boolean reservation; // 予約商品
public boolean isReservation() { return reservation; }
}