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

    private Long orderItemId;     // 注文詳細
    private Long orderId;         // 注文ID
    private String orderNumber;   // 注文番号
    private String memberNumber;  // 会員番号
    private String productId;     // 商品ID
    private Integer price;        // 単価
    private Integer quantity;     // 数量
    private Integer lineTotal;    // price × quantity（サーバー計算）
    private Double weight;        // 商品1個あたりの重量（EMS計算用）
    private String productName;   // 商品名
    private String imageLink;     // 商品画像URL
   private String createdBy;     
   private String updatedBy;
   private LocalDateTime createdAt;
   private LocalDateTime updatedAt;
}
