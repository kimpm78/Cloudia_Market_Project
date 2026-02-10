package com.cloudia.backend.CM_04_1000.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 注文詳細情報を確認するためのデータ転送
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewOrderCheckRequest {
    private String memberNumber;    // 会員番号
    private String orderNumber;     // 注文番号
    private Long productId;         // 商品ID
}
