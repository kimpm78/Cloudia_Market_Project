package com.cloudia.backend.CM_04_1001.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_04_1001.model.ReviewCommentRequest;
import com.cloudia.backend.CM_04_1001.model.ReviewCommentInfo;

import java.util.List;

@Mapper
public interface CM041001Mapper {

    /**
     * コメント／返信コメント登録（parentCommentId == null → コメント、値あり → 返信コメント）
     *
     * @param comment コメントリクエストデータ
     */
    void insertComment(ReviewCommentRequest comment);

    /**
     * ルートコメントのgroup_id補正
     */
    int updateRootGroupId(@Param("commentId") Long commentId);


    /**
     * 特定レビューのコメント一覧取得
     *
     * @param reviewId レビューID
     * @return コメント情報リスト
     */
    List<ReviewCommentInfo> selectCommentsByReviewId(@Param("reviewId") Long reviewId);

    /**
     * 親コメントの存在確認
     *
     * @param commentId 親コメントID
     * @return 存在可否（1: あり、0: なし）
     */
    int existsComment(@Param("commentId") Long commentId);

    /**
     * コメント作成者ID取得
     *
     * @param commentId コメントID
     * @return 作成者ID（存在しない場合はnull）
     */
    Long findCommentOwnerId(@Param("commentId") Long commentId);

    /**
     * コメント更新（本人のみ）
     *
     * @param commentId 更新対象コメントID
     * @param userId    作成者ID
     * @param content   更新内容
     * @return 更新件数
     */
    int updateComment(@Param("commentId") Long commentId,
        @Param("userId") Long userId,
        @Param("content") String content);

    /**
     * コメント削除（ソフトデリート、本人のみ）
     *
     * @param commentId 削除対象コメントID
     * @param userId    作成者ID（本人のみ削除可能）
     * @return 削除件数
     */
    int softDeleteComment(@Param("reviewId") Long reviewId,
        @Param("commentId") Long commentId,
        @Param("userId") Long userId);

    /**
     * レビュー削除時にコメント／返信コメントを一括ハード削除
     */
    int deleteCommentsByReviewId(@Param("reviewId") Long reviewId);
}
