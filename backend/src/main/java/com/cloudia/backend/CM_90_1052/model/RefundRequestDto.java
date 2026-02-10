package com.cloudia.backend.CM_90_1052.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
public class RefundRequestDto {
    private String requestNo;                   // 返金リクエスト番号
    private String refundNumber;                // 返金番号
    private String orderNumber;                 // 注文番号
    private String refundType;                  // 返金種別
    private String exchangeParts;               // 交換部品有無（Y/N）
    private List<RefundProductDto> products;    // 返金商品一覧
    private String shippingFee;                 // 配送料
    private Integer shippingAmount;             // 配送料金額
    private Integer productTotalAmount;         // 商品合計金額
    private Integer totalAmount;                // 合計金額
    private String memo;                        // メモ    

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundProductDto {
        private String productNumber;           // 商品番号
        private String productName;             // 商品名
        private Integer quantity;               // 数量
        private Integer unitPrice;              // 単価
        private Integer totalPrice;             // 合計金額
    }
}