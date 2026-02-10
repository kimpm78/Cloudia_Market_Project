package com.cloudia.backend.CM_04_1000.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {
    private Long userId;            // 作成者ID
    private String memberNumber;    // 注文 会員番号
    private String orderNumber;     // 注文番号
    private Long reviewId;          // レビューID

    @NotBlank(message = "商品コードを入力してください。")
    private String productCode;     // 商品コード
    private Long productId;         // 商品ID

    @NotBlank(message = "タイトルを入力してください。")
    private String title;           // タイトル
    private Integer reviewType;     // 0=レビュー、1=口コミ
    private String imageUrl;        // 添付画像

    @NotBlank(message = "内容を入力してください。")
    private String content;         // 本文
    private String createdBy;       // 作成者
    private String updatedBy;       // 更新者
}