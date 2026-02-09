package com.cloudia.backend.CM_04_1000.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 리뷰 작성 시 주문 + 상품 정보 DTO
 * - 주문 정보(orderId, memberNumber, orderNumber, orderDate)
 * - 상품 목록(products)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailResponse {

    // 주문 헤더
    private Long orderId;
    private String memberNumber; // 주문자 식별
    private String orderNumber;  // 주문번호 (예: '00001')
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    private String productCode; // 주문상세 코드
    private Long productId; // products PK
    private String productName;

    @Builder.Default
    private List<ReviewProduct> products = new ArrayList<>();
}