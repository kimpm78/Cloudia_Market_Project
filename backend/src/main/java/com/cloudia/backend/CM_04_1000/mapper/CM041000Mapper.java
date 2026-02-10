package com.cloudia.backend.CM_04_1000.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_04_1000.model.Attachments;
import com.cloudia.backend.CM_04_1000.model.OrderDetailResponse;
import com.cloudia.backend.CM_04_1000.model.ReviewInfo;
import com.cloudia.backend.CM_04_1000.model.ReviewRequest;

@Mapper
public interface CM041000Mapper {

    /**
     * レビュー一覧を取得（商品別、ページネーション対応）
     *
     * @param params パラメータマップ（productId, size, offset など）
     * @return レビュー一覧
     */
    List<ReviewInfo> selectReviews(java.util.Map<String, Object> params);

    /**
     * レビューを1件取得
     *
     * @param reviewId レビューID
     * @return レビュー詳細
     */
    ReviewInfo selectReviewById(@Param("reviewId") Long reviewId);

    /**
     * レビュー作成
     *
     * @param review 作成するレビューのリクエスト情報
     * @return 作成されたレビューID
     */
    Long writeReview(ReviewRequest review);

    /**
     * レビュー更新
     *
     * @param reviewId 更新対象のレビューID
     * @param review 更新するレビューのリクエスト情報
     * @return 更新件数（成功: 1, 失敗: 0）
     */
    int updateReview(@Param("reviewId") Long reviewId, 
        @Param("review") ReviewRequest review);

    /**
     * レビュー削除
     *
     * @param reviewId 削除対象のレビューID
     * @param userId 削除をリクエストしたユーザーID
     * @return 削除件数
     */
    int deleteReview(@Param("reviewId") Long reviewId, @Param("userId") Long userId);

    /**
     * レビュー閲覧数を+1
     *
     * @param reviewId 閲覧数を増加させるレビューID
     */
    int incrementViewCount(@Param("reviewId") Long reviewId);

    /**
     * 投稿者向け：注文＋商品一覧を取得
     *
     * @param memberNumber 会員番号
     * @return 注文＋商品一覧
     */
    List<OrderDetailResponse> selectOrdersWithProducts(@Param("memberNumber") String memberNumber);

    /**
     * 注文内に商品が存在するか確認（レビュー作成前の検証）
     *
     * @param memberNumber 会員番号
     * @param orderNumber 注文番号
     * @param productCode 商品コード
     * @return 存在する場合は1以上、存在しない場合は0
     */
    int checkOrderProduct(@Param("memberNumber") String memberNumber,
        @Param("orderNumber") String orderNumber,
        @Param("productCode") String productCode);

    /**
     * productCode から productId を取得（レビュー登録時の変換用）
     *
     * @param productCode 商品コード
     * @return 商品PK
     */
    Long findProductIdByCode(@Param("productCode") String productCode);

    /**
     * レビューのメイン画像を更新
     *
     * @param reviewId レビューID
     * @param imageUrl メイン画像URL
     * @return 更新件数
     */
    int updateReviewImage(@Param("reviewId") Long reviewId, @Param("imageUrl") String imageUrl);
        
    /**
     * レビューエディター画像を登録
     *
     * @param attachment 登録する添付画像情報
     * @return 登録件数
     */
    int insertReviewAttachment(Attachments attachment);

    /**
     * レビューエディター画像を更新
     *
     * @param attachment 更新する添付画像情報
     * @return 更新件数
     */
    // int updateReviewAttachment(Attachments attachment);

    /**
     * レビューエディター画像を取得
     *
     * @param reviewId レビューID
     * @return 添付画像一覧
     */
    List<Attachments> selectReviewAttachments(@Param("reviewId") Long reviewId);

    /**
     * レビューエディター画像を削除
     *
     * @param imageId 添付画像ID
     * @param reviewId レビューID
     * @return 削除件数
     */
    int deleteReviewAttachment(@Param("imageId") Long imageId, @Param("reviewId") Long reviewId);

    /**
     * レビュー本文を更新
     *
     * @param reviewId レビューID
     * @param content  更新後のHTMLコンテンツ
     * @param updatedBy 更新者
     * @return 更新件数
     */
    int updateReviewContent(@Param("reviewId") Long reviewId,
        @Param("content") String content,
        @Param("updatedBy") String updatedBy);

}