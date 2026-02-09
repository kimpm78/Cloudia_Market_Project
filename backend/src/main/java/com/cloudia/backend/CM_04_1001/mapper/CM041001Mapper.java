package com.cloudia.backend.CM_04_1001.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_04_1001.model.ReviewCommentRequest;
import com.cloudia.backend.CM_04_1001.model.ReviewCommentInfo;

import java.util.List;

@Mapper
public interface CM041001Mapper {

    /**
     * 댓글 / 대댓글 등록 (parentCommentId == null → 댓글, 값 있으면 대댓글)
     *
     * @param comment 댓글 요청 데이터
     */
    void insertComment(ReviewCommentRequest comment);

    /**
     * 루트 댓글의 group_id 보정
     */
    int updateRootGroupId(@Param("commentId") Long commentId);


    /**
     * 특정 리뷰의 댓글 목록 조회
     *
     * @param reviewId 리뷰 ID
     * @return 댓글 정보 리스트
     */
    List<ReviewCommentInfo> selectCommentsByReviewId(@Param("reviewId") Long reviewId);

    /**
     * 부모 댓글 존재 여부 확인
     *
     * @param commentId 부모 댓글 ID
     * @return 존재 여부(1: 존재, 0: 없음)
     */
    int existsComment(@Param("commentId") Long commentId);

    /**
     * 댓글 작성자 ID 조회
     *
     * @param commentId 댓글 ID
     * @return 작성자 ID (없으면 null)
     */
    Long findCommentOwnerId(@Param("commentId") Long commentId);

    /**
     * 댓글 수정 (본인만 가능)
     *
     * @param commentId 수정할 댓글 ID
     * @param userId    작성자 ID
     * @param content   수정할 내용
     * @return 수정된 행 수
     */
    int updateComment(@Param("commentId") Long commentId,
        @Param("userId") Long userId,
        @Param("content") String content);

    /**
     * 댓글 삭제 (Soft Delete, 본인만 가능)
     *
     * @param commentId 삭제할 댓글 ID
     * @param userId    작성자 ID (본인만 삭제 가능)
     * @return 삭제된 행 수
     */
    int softDeleteComment(@Param("reviewId") Long reviewId,
        @Param("commentId") Long commentId,
        @Param("userId") Long userId);

    /**
     * 리뷰 삭제 시 댓글/대댓글 일괄 하드 삭제
     */
    int deleteCommentsByReviewId(@Param("reviewId") Long reviewId);
}
