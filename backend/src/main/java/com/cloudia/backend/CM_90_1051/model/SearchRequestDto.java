package com.cloudia.backend.CM_90_1051.model;

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
public class SearchRequestDto {
    private String dateFrom;        // 開始日
    private String dateTo;          // 終了日
    private int paymentMethod;      // 支払方法
    private String memberNumber;    // 会員ID
    private String orderNumber;     // 注文ID
    private int orderStatusValue;   // ステータス
    private String carrier;         // 配送会社
    private String trackingNumber;  // 追跡番号
}