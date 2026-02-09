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
public class OrderInfo {

   /**
    * 기본 주문 정보
    */
   private Long orderId; // PK
   private String orderNumber; // 주문번호
   private String memberNumber; // 회원번호
   private LocalDateTime orderDate; // 주문 시각
   private LocalDateTime createdAt;
   private LocalDateTime updatedAt;

   /**
    * 금액 정보
    */
   private Integer subtotal;         // 상품가격 총합
   private Integer shippingCost;     // 배송비
   private Integer totalAmount;      // 결제금액 (subtotal + shippingCost)
   private Long discountAmount;      // 할인액(옵션)

   /**
    * 주문 상태 (정산 코드: 008)
    */
   private String orderStatusType;   // 항상 "008"
   private Integer orderStatusValue; // 1~7 상태 값
   private Integer paymentValue; // 1=계좌이체, 2=신용카드
   private List<OrderItemInfo> items; // 주문 상품 목록

   /**
    * 결제 정보
    */
   private String paymentMethod; // 카드/계좌 등
   private String pgTid; // PG 거래번호 (TID)
   private String paymentType;

   /**
    * 배송 정보
    */
   private String recipientName;
   private String recipientPhone;
   private String zipCode;
    private String address;           // 전체 주소
    private Long shippingAddressId;   // (옵션 테이블 구조 시 사용)

   /**
    * 감사 정보
    */
   private String createdBy;
   private String updatedBy;
}
