package com.cloudia.backend.CM_04_1001.model;

import java.util.List;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReviewCommentInfo {
    private Long commentId;         // コメントID
    private Long reviewId;          // 親レビューID
    private Long userId;            // コメント作成者ID
    private Long parentCommentId;   // 親コメントID（nullの場合は最上位コメント）
    private String content;         // コメント本文
    private Integer deleteFlag;     // 削除フラグ（0: 有効、1: 削除）
    private String createdBy;       // 登録者
    private String createdAt;       // 登録日
    private String updatedBy;       // 更新者
    private String updatedAt;       // 更新日
    @Builder.Default
    private List<ReviewCommentInfo> children = new ArrayList<>(); // 返信コメント一覧
}