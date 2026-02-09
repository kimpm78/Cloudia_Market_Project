package com.cloudia.backend.CM_90_1060.model;

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
public class Stock {
    private int stockId; // 재고 ID
    private String productCode; // 상품 코드
    private int price; // 상품 가격
    private String productName; // 상품명
    private int defectiveQty; // 불량 재고
    private int totalQty; // 총 재고
    private int availableQty; // 가용 재고
    private String productCategory; // 상품 분류
    private String createdBy; // 등록자
    private LocalDateTime createdAt; // 등록일
    private String updatedBy; // 수정자
    private LocalDateTime updatedAt; // 수정일
}
