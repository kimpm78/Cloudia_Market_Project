package com.cloudia.backend.CM_06_1000.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 장바구니 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long cartItemId; // 장바구니아이템ID
    private Long userId; // 사용자ID
    private String productId; // 상품ID
    private Integer quantity; // 1 이상
    private Integer isActive; // 1=유효, 0=삭제(소프트 삭제)
    private LocalDateTime cartUpdatedAt; // 수정일시
    private String createdBy; // 생성자
    private LocalDateTime createdAt; // 생성일시
    private String updatedBy; // 수정자
    private LocalDateTime updatedAt; // 수정일시
}