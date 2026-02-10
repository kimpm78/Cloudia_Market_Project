package com.cloudia.backend.CM_03_1000.model;

import java.time.LocalDateTime;
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
public class ProductInfo {
    private Long productId;               // 商品ID
    private String productCode;           // 商品コード
    private String name;                  // 商品名
    private int price;                    // 商品価格
    private int deliveryPrice;            // 送料
    private String releaseDate;           // 出荷日（YYYY-MM-DD）
    private String reservationDeadline;   // 予約締切日（YYYY-MM-DD）
    private String estimatedDeliveryDate; // 通常販売の配送予定日（サーバー計算）
    private int codeValue;                // コード値
    private String category;              // カテゴリー
    private String categoryGroupName;     // カテゴリーグループ名
    private String createdBy;             // 登録者
    private LocalDateTime createdAt;      // 登録日
    private String updatedBy;             // 更新者
    private LocalDateTime updatedAt;      // 更新日
    private String thumbnailUrl;          // サムネイル画像
    private String description;           // 商品説明
    private Double weight;                // 重量
    private Integer availableQty;         // 利用可能在庫数（JOIN/計算用）
    private Integer purchaseLimit;        // 最大購入数
    private List<String> detailImages;    // 詳細画像リスト
}
