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
public class Products {
    private Long productId; // 상품ID
    private String productCode; // 상품 코드
    private String name; // 상품명
    private int price; // 상품 가격
    private int deliveryPrice; // 배달비
    private String releaseDate; // 출고일
    private String reservationDeadline; // 예약마감일
    private int codeValue; // 코드 값
    private String category; // 카테고리
    private int purchaseLimit; // 구매 수량
    private double weight; // 상품 무게
    private String createdBy; // 등록자
    private LocalDateTime createdAt; // 등록일
    private String updatedBy; // 수정자
    private LocalDateTime updatedAt; // 수정일
}
