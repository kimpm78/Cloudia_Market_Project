package com.cloudia.backend.CM_90_1060.model;

import java.math.BigDecimal;
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
public class ResponseProducts {
    private Long productId;// 상품 아이디
    private String productCode;// 상품 코드
    private String name;// 상품 명
    private String categoryGroupName; // 카테고리 이름
    private String category; // 카데고리 코드
    private BigDecimal price;// 상품 가격
    private String codeValue;// 코드
    private Integer availableQty;// 재고
    private String reservationDeadline; // 예약 마감일
    private LocalDateTime createdAt;// 등록일
    private String createdBy;// 등록자
    private LocalDateTime updatedAt;// 업데이트일
    private String updatedBy;// 업데이트자
}
