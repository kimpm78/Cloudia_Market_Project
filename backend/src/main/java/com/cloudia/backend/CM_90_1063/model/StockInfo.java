package com.cloudia.backend.CM_90_1063.model;

import java.time.LocalDateTime;

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
public class StockInfo {
    private int stockDetailId; // 재고 ID
    private String productCode; // 상품 코드
    private String productName; // 상품명
    private int qty; // 수량
    private String reason; // 비고
    private String createdBy; // 등록자
    private LocalDateTime createdAt; // 등록일
}
