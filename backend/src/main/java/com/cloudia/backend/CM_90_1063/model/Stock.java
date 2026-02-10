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
public class Stock {
    private int stockId;                // 在庫ID
    private String productCode;         // 商品コード
    private int price;                  // 商品価格
    private int defectiveQty;           // 不良在庫
    private int totalQty;               // 総在庫
    private int availableQty;           // 利用可能在庫
    private String productCategory;     // 商品分類
    private String createdBy;           // 登録者
    private LocalDateTime createdAt;    // 登録日
    private String updatedBy;           // 更新者
    private LocalDateTime updatedAt;    // 更新日
}
