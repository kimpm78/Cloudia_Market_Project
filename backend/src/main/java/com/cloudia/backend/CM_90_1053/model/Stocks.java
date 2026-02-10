package com.cloudia.backend.CM_90_1053.model;

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
public class Stocks {
    private String productCode;      // 商品コード
    private String productName;      // 商品名
    private int productCategory;     // 商品カテゴリ
    private int totalQty;            // 総在庫
    private int cartQty;             // カート内総在庫
    private int defectiveQty;        // 不良在庫
    private int availableQty;        // 利用可能在庫
    private int saleStatus;          // ステータス
    private String createdBy;        // 登録者
    private LocalDateTime createdAt; // 登録日
}
