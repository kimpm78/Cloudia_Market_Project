package com.cloudia.backend.CM_06_1000.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * カートDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long cartItemId;            // カートID
    private Long userId;                // ユーザーID
    private String productId;           // 商品ID
    private Integer quantity;           // 1以上
    private Integer isActive;           // 1=有効、0=削除（論理削除）
    private LocalDateTime cartUpdatedAt;// 更新日時
    private String createdBy;           // 作成者
    private LocalDateTime createdAt;    // 作成日時
    private String updatedBy;           // 更新者
    private LocalDateTime updatedAt;    // 更新日時
}