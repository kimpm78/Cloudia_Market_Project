package com.cloudia.backend.batch.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrdersDto {
    private String orderNumber;       // 注文番号
    private String memberNumber;      // 会員番号
    private String recipientName;     // 注文者名
    private int totalAmount;          // 合計金額
    private LocalDateTime orderDate;  // 注文日
    private LocalDateTime endDate;    // 注文締切日
}
