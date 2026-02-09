package com.cloudia.backend.CM_04_1000.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 주문 세부 정보를 확인하기 위한 데이터 전송 객체
 * 회원 번호, 주문 번호 및 제품 ID 정보가 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewOrderCheckRequest {
    private String memberNumber; // 회원 번호
    private String orderNumber;  // 주문번호 (varchar)
    private Long productId;    // 상품 ID (varchar)
}
