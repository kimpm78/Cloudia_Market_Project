package com.cloudia.backend.CM_90_1063.model;

import java.time.LocalDateTime;

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
public class StockInfo {
    private int stockDetailId;          // 在庫ID
    private String productCode;         // 商品コード
    private String productName;         // 商品名
    private int qty;                    // 数量
    private String reason;              // 備考
    private String createdBy;           // 登録者
    private LocalDateTime createdAt;    // 登録日
}
