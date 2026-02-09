package com.cloudia.backend.CM_06_1001.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreate {    
    private Long userId; // 주문자 user_id (cart_items.user_id)
    private String memberNumber; // 주문자 회원 번호
    private List<Long> cartItemIds; // 장바구니에서 선택된 항목
    private Integer shippingFee; // 배송비
    private String shippingArea; // 배송 구간
    private String paymentMethod; // 선택: 결제 수단
    private ShippingInfo shipping; // 배송 정보 전체 묶음
    private Integer totalAmount; // 서버 계산용 (cart + shipping)
    private String createdBy; // 등록자
    private LocalDateTime createdAt; // 등록일
    private String updatedBy; // 수정자
    private LocalDateTime updatedAt; // 수정일
}
