package com.cloudia.backend.CM_06_1000.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * カート追加検証用の商品メタ情報
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartProductMeta {
    private String productId;       // 商品コード
    private String releaseMonth;    // 発売月
    private Boolean reservation;    // 予約商品 有無
    private Integer purchaseLimit;  // 最大購入数量
}
