package com.cloudia.backend.CM_04_1001.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 댓글 요청
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCommentRequest {
    private Long reviewId;         // 부모 리뷰 ID
    private Long parentCommentId;  // 부모 댓글 ID (대댓글인 경우 사용, 없으면 null)
    private Long commentId;        // 댓글 ID (수정/삭제 시 사용)
    private Long userId;           // 댓글 작성자 ID
    private String content;        // 댓글 본문
    private String createdBy;      // 등록자
}