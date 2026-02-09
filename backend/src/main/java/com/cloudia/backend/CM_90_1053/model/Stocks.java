package com.cloudia.backend.CM_90_1053.model;

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
public class Stocks {
    private String productCode; // 상품 코드
    private String productName; // 상품명
    private int productCategory; // 상품 카테고리
    private int totalQty; // 총 재고
    private int cartQty; // 장바구니 총 재고
    private int defectiveQty; // 불량 재고
    private int availableQty; // 가용 재고
    private int saleStatus; // 상태
    private String createdBy; // 등록자
    private LocalDateTime createdAt; // 등록일
}
