package com.cloudia.backend.CM_06_1000.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 장바구니 담기 검증용 상품 메타 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartProductMeta {
    private String productId; // 상품 코드
    private String releaseMonth; // 출시월
    private Boolean reservation; // 예약상품 여부
    private Integer purchaseLimit; // 최대 구매 수량
}
