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

   private Long orderId;
   private String orderNumber;
   private Integer subtotal;           // 商品合計金額
   private Integer shippingCost;       // 配送料（EMS計算結果）
   private Integer totalAmount;        // 最終決済金額
   private Integer orderStatusValue;   // ステータスコード値
    private String orderStatusText;    // 画面表示用文字列（例：決済待ち／決済完了／配送準備中）
   private String buyerName;
   private String buyerEmail;
   private String recipientName;
   private String recipientPhone;
   private Long shippingAddressId;
   private String address;
   private ShippingInfo shipping;
   private List<OrderItemInfo> items;
}
