package com.cloudia.backend.CM_06_1001.model;

import java.io.Serializable;
import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummary implements Serializable {

   private static final long serialVersionUID = 1L;

   /**
    * 주문 기본 정보
    */
   private Long orderId;
   private String orderNumber;

   /**
    * 금액 정보
    */
   private Integer subtotal;       // 상품 총액
    private Integer shippingCost;   // 배송비 (OrderInfo.shippingCost와 통일)
    private Integer totalAmount;    // 최종 결제 금액

   /**
    * 주문 상태
    */
   private Integer orderStatusValue; // 상태 코드 값 (1~7)
    private String orderStatusText;   // 화면 표시용 문자열 (예: 결제대기/결제완료/배송준비중)

   /**
    * 구매자 정보 (UI 표시용)
    */
   private String buyerName;
   private String buyerEmail;

   /**
    * 배송/수령인 정보
    */
   private String recipientName;
   private String recipientPhone;
   private Long shippingAddressId;
   private String address;

   /**
    * 배송 정보
    */
   private ShippingInfo shipping;

   /**
    * 주문 상품 목록
    */
   private List<OrderItemInfo> items;
}
