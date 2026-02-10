package com.cloudia.backend.CM_90_1050.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ResultDto {
    private String memberNumber;     // 社員番号
    private String loginId;          // ログインID
    private String productName;      // 商品名
    private Integer totalAmount;     // 精算金額
    private Integer unitPrice;       // 単価
    private Integer shippingCost;    // 配送料
    private Integer discountAmount;  // 割引金額
    private Integer price;           // 仕入価格
    private LocalDateTime orderDate; // 注文日
    private Integer quantity;        // 購入数量
}