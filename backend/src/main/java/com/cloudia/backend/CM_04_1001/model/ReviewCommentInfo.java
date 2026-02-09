package com.cloudia.backend.CM_04_1001.model;

import java.util.List;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * 리뷰 댓글 정보 (comments 테이블 기반)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReviewCommentInfo {
    private Long commentId;     // 댓글 ID
    private Long reviewId;      // 부모 리뷰 ID
    private Long userId;        // 댓글 작성자 ID
    private Long parentCommentId; // 부모 댓글 ID (null이면 최상위 댓글)
    private String content;     // 댓글 본문
    private Integer deleteFlag;   // 삭제 여부 (0: 정상, 1: 삭제)
    private String createdBy;   // 등록자
    private String createdAt;   // 등록일
    private String updatedBy;   // 수정자
    private String updatedAt;   // 수정일
    @Builder.Default
    private List<ReviewCommentInfo> children = new ArrayList<>(); // 대댓글 목록
}