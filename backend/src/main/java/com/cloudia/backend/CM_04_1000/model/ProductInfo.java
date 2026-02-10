package com.cloudia.backend.CM_04_1000.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfo {
    private String orderNumber;  // 注文番号
    private String productCode;  // 商品コード
    private Long productId;      // 商品PK
    private String productName;  // 商品名
}