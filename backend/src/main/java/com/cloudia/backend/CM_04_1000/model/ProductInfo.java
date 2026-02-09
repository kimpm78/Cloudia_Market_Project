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
    private String orderNumber;  // 주문 번호 (reviews와 연결하기 위해 추가)
    private String productCode;  // order_details.product_id (상품코드)
    private Long productId;      // products.product_id (PK)
    private String productName;  // products.name
}
