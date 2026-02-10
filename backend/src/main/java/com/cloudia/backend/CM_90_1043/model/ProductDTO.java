package com.cloudia.backend.CM_90_1043.model;

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
public class ProductDTO {
    private Long productId;             // 商品ID
    private String productCode;         // 商品コード
    private String name;                // 商品名
    private int price;                  // 商品価格
    private int deliveryPrice;          // 配送料
    private String releaseDate;         // 出荷日
    private String reservationDeadline; // 予約締切日
    private int codeValue;              // コード値
    private String category;            // カテゴリ
    private int purchaseLimit;          // 購入数量
    private String createdBy;           // 登録者
    private LocalDateTime createdAt;    // 登録日
    private String updatedBy;           // 更新者
    private LocalDateTime updatedAt;    // 更新日
}
