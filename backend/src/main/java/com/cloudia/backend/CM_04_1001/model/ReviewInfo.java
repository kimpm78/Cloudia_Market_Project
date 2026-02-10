package com.cloudia.backend.CM_04_1001.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ReviewInfo {
    private Long reviewId;                      // レビューID
    private Long productId;                     // 商品ID
    private Long userId;                        // 作成者ID
    private String userName;                    // 作成者名
    private String title;                       // レビュータイトル
    private String content;                     // レビュー内容
    private String createdAt;                   // 作成日
    private String updatedAt;                   // 更新日
    private List<ReviewCommentInfo> comments;   // 詳細に含まれるコメント/返信コメント一覧
}
