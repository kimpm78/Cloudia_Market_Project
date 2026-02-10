package com.cloudia.backend.CM_04_1000.model;

import com.cloudia.backend.CM_04_1001.model.ReviewCommentInfo;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

/**
 * レビュー＋詳細／一覧取得に使用するレスポンス
 */
@Getter
@Setter
public class ReviewInfo {
    private Long reviewId;                              // レビューID
    private Long userId;                                // 作成者ID
    private Long productId;                             // 商品ID
    private String productName;                         // 商品名
    private Long orderId;                               // 注文ID
    private String orderNumber;                         // 注文番号（例: '00001'）
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;                    // 注文日（YYYY-MM-DD）
    private String productCode;                         // 注文詳細コード
    private Integer reviewType;                         // 0=レビュー、1=口コミ
    private String title;                               // レビュータイトル
    private String imageUrl;                            // 添付画像
    private String content;                             // レビュー内容
    private Integer viewCount = 0;                      // 閲覧数
    private String createdBy;                           // 登録者
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;                    // 登録日
    private String updatedBy;                           // 更新者
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;                    // 更新日

    private java.util.List<ReviewCommentInfo> comments; // コメント一覧
}