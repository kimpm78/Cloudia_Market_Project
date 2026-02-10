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

   private Long orderId;               // 注文ID（PK）
   private String orderNumber;         // 注文番号
   private String memberNumber;        // 会員番号
   private LocalDateTime orderDate;    // 注文日時
   private LocalDateTime createdAt;
   private LocalDateTime updatedAt;
   private Integer subtotal;           // 商品価格合計
   private Integer shippingCost;       // 配送料
   private Integer totalAmount;        // 決済金額（subtotal + shippingCost）
   private Long discountAmount;        // 割引額（オプション）
   private String orderStatusType;     // 常に "008"
   private Integer orderStatusValue;   // 1～7 のステータス値
   private Integer paymentValue;       // 1=銀行振込、2=クレジットカード
   private List<OrderItemInfo> items;  // 注文商品一覧
   private String paymentMethod;       // カード／銀行振込など
   private String pgTid;               // PG取引番号（TID）
   private String paymentType;
   private String recipientName;
   private String recipientPhone;
   private String zipCode;
   private String address;             // 住所（全文）
   private Long shippingAddressId;     // （オプション：テーブル構造使用時）
   private String createdBy;
   private String updatedBy;
}
