package com.cloudia.backend.CM_04_1001.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCommentRequest {
    private Long reviewId;         // 親レビューID
    private Long parentCommentId;  // 親コメントID（返信コメントの場合に使用、なければnull）
    private Long commentId;        // コメントID（更新／削除時に使用）
    private Long userId;           // コメント作成者ID
    private String content;        // コメント本文
    private String createdBy;      // 登録者
}