package com.cloudia.backend.CM_90_1060.model;

import java.math.BigDecimal;
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
public class ResponseProducts {
    private Long productId;             // 商品ID
    private String productCode;         // 商品コード
    private String name;                // 商品名
    private String categoryGroupName;   // カテゴリ名
    private String category;            // カテゴリコード
    private BigDecimal price;           // 商品価格
    private String codeValue;           // コード
    private Integer availableQty;       // 在庫
    private String reservationDeadline; // 予約締切日
    private LocalDateTime createdAt;    // 登録日
    private String createdBy;           // 登録者
    private LocalDateTime updatedAt;    // 更新日
    private String updatedBy;           // 更新者
}
