package com.cloudia.backend.CM_06_1000.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * カート取得・注文準備レスポンスDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(Include.NON_NULL)
public class CartItemResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long cartItemId;            // カートアイテム固有ID
    private String productId;           // 商品固有ID
    private String productName;         // 商品名
    private Integer productPrice;       // 商品価格（注文時点の単価、ウォン単位）
    private String imageLink;           // 商品画像リンク
    private Integer quantity;           // 数量
    private Integer lineTotal;          // 合計金額（商品価格 × 数量、ウォン単位）
    private Integer shippingFee;        // 配送料（ウォン単位）
    private Double productWeight;       // 商品重量（kg単位）
    private String reservationDeadline; // 予約締切日（YYYY-MM-DD）
    private String releaseDate;         // 発売日（YYYY-MM-DD）
    private Integer purchaseLimit;      // 最大購入数量
}