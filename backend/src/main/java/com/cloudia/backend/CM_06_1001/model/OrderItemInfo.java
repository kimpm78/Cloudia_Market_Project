package com.cloudia.backend.CM_06_1001.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderItemInfo implements Serializable {

   private static final long serialVersionUID = 1L;

    // 기본 키 / 참조 키
    private Long orderItemId;     // 주문 상세 PK (order_details.order_item_id)
    private Long orderId;         // 주문 ID (FK, orders.order_id)
    private String orderNumber;   // 주문 번호 (order_details.order_number)
    private String memberNumber;  // 회원 번호 (order_details.member_number)

    // 상품 및 금액 스냅샷
    private String productId;     // 상품 ID/코드
    private Integer price;        // 단가(스냅샷)
    private Integer quantity;     // 수량
    private Integer lineTotal;    // price * quantity (서버 계산)

    // 국제 배송/부피 계산용
    private Double weight;        // 상품 1개 기준 중량(EMS 계산용)

    // 화면/프론트 표시용 스냅샷
    private String productName;   // 상품명 (스냅샷)
    private String imageLink;     // 상품 이미지 URL

    // 감사(Audit) 정보
   private String createdBy;     
   private String updatedBy;
   private LocalDateTime createdAt;
   private LocalDateTime updatedAt;
}
