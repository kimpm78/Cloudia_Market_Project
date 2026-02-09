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
    private Long productId;      // 상품 PK
    private String productCode;  // 상품 코드
    private String productName;  // 상품명
    private String thumbnailUrl; // 썸네일
}