package com.cloudia.backend.CM_06_1000.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 장바구니 조회/주문 준비 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(Include.NON_NULL)
public class CartItemResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long cartItemId; // 장바구니 아이템 고유 아이디
    private String productId; // 상품 고유 아이디
    private String productName; // 상품명
    private Integer productPrice; // 상품 가격(주문 시점 단가, 원화 단위)
    private String imageLink; // 상품 이미지 링크
    private Integer quantity; // 수량
    private Integer lineTotal; // 합계 금액(상품 가격 * 수량, 원화 단위)
    private Integer shippingFee; // 배송비(원화 단위)
    private Double productWeight; // 상품 무게(kg 단위)
    private String reservationDeadline; // 예약 마감일 (YYYY-MM-DD)
    private String releaseDate; // 출시일 (YYYY-MM-DD)
    private Integer purchaseLimit; // 최대 구매 수량
}