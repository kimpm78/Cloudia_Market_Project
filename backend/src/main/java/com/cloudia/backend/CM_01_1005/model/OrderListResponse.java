package com.cloudia.backend.CM_01_1005.model;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListResponse {
    private String orderNo;             // 注文番号
    private String orderDate;           // 注文日
    private String productName;         // 商品名
    private String deliveryDate;        // 発送予定日
    private Long totalPrice;            // 金額／決済方法
    private int paymentValue;
    private String orderStatus;         // 状態
    private int orderStatusValue;       // 状態値
    private LocalDateTime paymentAt;    // 72時間計算の基準となる時刻
    private String productImageUrl;
}