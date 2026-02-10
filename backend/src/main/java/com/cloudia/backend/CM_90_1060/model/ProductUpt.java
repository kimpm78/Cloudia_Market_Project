package com.cloudia.backend.CM_90_1060.model;

import java.util.List;

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
public class ProductUpt {
    private String category;                // カテゴリ
    private String productCode;             // 商品コード
    private String productCategory;         // 商品区分
    private String productName;             // 商品名
    private int productPrice;               // 商品価格
    private int deliveryPrice;              // 配送料
    private String purchasePrice;           // 仕入価格
    private int purchaseLimit;              // 購入数量
    private String expectedDeliveryDate;    // 出荷予定日
    private String reservationDeadline;     // 予約締切日
    private String productFile;             // 商品画像
    private List<String> detailImages;      // 詳細画像
    private String productnote;             // 商品説明
    private Double weight;                  // 重量
    private int availableQty;               // 利用可能在庫
}
