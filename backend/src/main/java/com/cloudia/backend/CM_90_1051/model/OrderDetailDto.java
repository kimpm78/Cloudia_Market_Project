package com.cloudia.backend.CM_90_1051.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDto {
    private Integer orderDetailId;  // 注文詳細ID
    private String productName;     // 商品名
    private String quantity;        // 数量
    private String totalPrice;      // 合計金額
    private String unitPrice;       // 単価
}
