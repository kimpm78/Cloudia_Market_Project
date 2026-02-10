package com.cloudia.backend.CM_04_1000.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewProduct {
    private Long productId;      // 商品PK
    private String productCode;  // 商品コード
    private String productName;  // 商品名
    private String thumbnailUrl; // サムネイル
}