package com.cloudia.backend.CM_04_1001.service;

import java.util.List;

import com.cloudia.backend.CM_04_1001.model.ReviewCommentInfo;
import com.cloudia.backend.CM_04_1001.model.ReviewCommentRequest;

public interface CM041001Service {


    /**
     * コメント一覧取得（ツリー構造の返信コメントを含む）
     *
     * @param reviewId レビューID
     * @return コメント・返信コメントを含むリスト（ツリー構造）
     */
    List<ReviewCommentInfo> getCommentsByReviewId(Long reviewId);

    /**
     * コメント／返信コメント登録
     * - parentCommentId がnullの場合は通常コメント、それ以外は返信コメント
     * - ログイン必須（userId必須）
     *
     * @param commentRequest コメント登録リクエスト
     * @return 登録したコメントID
     */
    Long saveComment(ReviewCommentRequest commentRequest);

    /**
     * コメント更新
     * - ログイン必須（userId必須）
     * - 作成者本人のみ更新可能
     *
     * @param reviewCommentId 更新対象コメントID
     * @param userId 作成者ユーザーID
     * @param content 更新内容
     * @return 更新成功可否
     */
    boolean updateComment(Long reviewCommentId, Long userId, String content);

    /**
     * コメント削除（論理削除・ソフトデリート）
     * - ログイン必須（userId必須）
     * - 作成者本人のみ削除可能
     *
     * @param reviewId レビューID
     * @param commentId 削除対象コメントID
     * @param userId 作成者ユーザーID
     * @return 削除成功可否
     */
    boolean softDeleteComment(Long reviewId, Long commentId, Long userId);
}