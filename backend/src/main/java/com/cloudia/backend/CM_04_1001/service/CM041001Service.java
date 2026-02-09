package com.cloudia.backend.CM_04_1001.service;

import java.util.List;

import com.cloudia.backend.CM_04_1001.model.ReviewCommentInfo;
import com.cloudia.backend.CM_04_1001.model.ReviewCommentRequest;

public interface CM041001Service {


    /**
     * 댓글 목록 조회 (트리 구조의 대댓글 포함)
     *
     * @param reviewId 리뷰 ID
     * @return 댓글 및 대댓글을 포함한 리스트 (트리 구조)
     */
    List<ReviewCommentInfo> getCommentsByReviewId(Long reviewId);

    /**
     * 댓글 및 대댓글 등록
     * - parentCommentId 가 null 이면 일반 댓글, 아니면 대댓글
     * - 로그인 필수 (userId 필수)
     *
     * @param commentRequest 댓글 등록 요청
     * @param parentCommentId 부모 댓글 ID (null 가능)
     * @return 등록 성공 여부
     */
    Long saveComment(ReviewCommentRequest commentRequest);

    /**
     * 댓글 수정
     * - 로그인 필수 (userId 필수)
     * - 작성자 본인만 수정 가능
     *
     * @param reviewCommentId 수정할 댓글 ID
     * @param userId 작성자 사용자 ID
     * @param content 수정할 내용
     * @return 수정 성공 여부
     */
    boolean updateComment(Long reviewCommentId, Long userId, String content);

    /**
     * 댓글 삭제 (논리 삭제, 소프트 삭제)
     * - 로그인 필수 (userId 필수)
     * - 작성자 본인만 삭제 가능
     *
     * @param reviewCommentId 삭제할 댓글 ID
     * @param userId 작성자 사용자 ID
     * @return 삭제 성공 여부
     */
    boolean softDeleteComment(Long reviewId, Long commentId, Long userId);
}