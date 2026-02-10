package com.cloudia.backend.CM_04_1000.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.cloudia.backend.CM_04_1000.model.OrderDetailResponse;
import com.cloudia.backend.CM_04_1000.model.ReviewInfo;
import com.cloudia.backend.CM_04_1000.model.ReviewRequest;
import com.cloudia.backend.common.model.ResponseModel;

public interface CM041000Service {

    /**
     * レビュー単件取得
     *
     * @param reviewId レビューID
     * @return レビュー情報（存在しない場合はnull）
     */
    ReviewInfo findReviewById(Long reviewId);

    /**
     * 商品別レビュー一覧取得（ページネーション）
     *
     * @param productId 商品ID
     * @param page ページ番号（0から開始）
     * @param size ページサイズ
     * @return レビュー一覧
     */
    List<ReviewInfo> findReviewsByProduct(Long productId, Integer page, Integer size);

    /**
     * レビュー全件取得
     *
     * @return レビュー一覧
     */
    List<ReviewInfo> findAllReviews();

    /**
     * レビュー削除（論理削除）
     *
     * @param reviewId レビューID
     * @param userId ログインユーザーID
     * @return 削除結果（ResponseModel含む：削除件数、成功可否、メッセージ）
     */
    ResponseEntity<ResponseModel<Integer>> deleteReview(Long reviewId, Long userId);

    /**
     * レビュー閲覧数加算（1日1回制限）
     *
     * @param reviewId レビューID
     * @param userId ログインユーザーID（未ログイン時はnull）
     * @param viewerKey 重複チェック用識別子（userId または未ログインIPベース）
     * @return 閲覧数が増加した場合はtrue、制限によりスキップした場合はfalse
     */
    boolean increaseViewOncePerDay(Long reviewId, Long userId, String viewerKey);

    /**
     * 特定会員の注文＋商品一覧取得
     *
     * @param memberNumber 会員番号
     * @return 注文＋商品一覧（注文単位、productsリスト含む）
     */
    List<OrderDetailResponse> findOrdersWithProducts(String memberNumber);

    /**
     * 注文内の商品存在チェック
     *
     * @param memberNumber 会員番号
     * @param orderNumber 注文番号
     * @param productCode 商品コード
     * @return 存在する場合はtrue、存在しない場合はfalse
     */
    boolean checkOrderProduct(String memberNumber, String orderNumber, String productCode);

    /**
     * レビュー登録（MultipartFile含む）
     *
     * @param entity レビューリクエスト情報
     * @param file アップロード画像
     * @return 登録結果（ResponseModel含む：生成されたレビューID、成功可否、メッセージ）
     */
    ResponseEntity<ResponseModel<Long>> createReviewWithImage(ReviewRequest entity, MultipartFile file);

    /**
     * レビュー更新（MultipartFile含む）
     *
     * @param entity レビューリクエスト情報
     * @param file アップロード画像
     * @return 更新成功可否（true: 成功、false: 失敗）
     */
    ResponseEntity<ResponseModel<Boolean>> updateReviewWithImage(ReviewRequest entity, MultipartFile file);

    /**
     * レビュー本文エディター画像アップロード（仮／更新共用）
     *
     * @param reviewId レビューID（null または 0 の場合は仮アップロード）
     * @param file アップロード画像
     * @return アップロード画像URLを含むResponseModel
     */
    ResponseEntity<ResponseModel<String>> uploadReviewEditorImage(Long reviewId, MultipartFile file);

    /**
     * レビューメイン画像アップロード（サムネイル）
     *
     * @param reviewId レビューID
     * @param file アップロードするメイン画像
     * @return アップロードされた画像URL
     */
    ResponseEntity<ResponseModel<String>> uploadReviewMainImage(Long reviewId, MultipartFile file);

    /**
     * レビューエディター画像削除（imageIdベース）
     *
     * @param reviewId レビューID
     * @param imageId レビュー画像PK
     * @return 削除成功可否
     */
    boolean deleteReviewEditorImage(Long reviewId, Long imageId);

    /**
     * レビュー画像削除（imageIdベース）
     *
     * @param reviewId レビューID
     * @param imageId レビュー画像PK
     * @return 削除成功可否
     */
    boolean deleteReviewImage(Long reviewId, Long imageId);
}