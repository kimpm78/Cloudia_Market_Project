package com.cloudia.backend.CM_04_1000.constants;

public class CM041000MessageConstant {
    private CM041000MessageConstant() {
    }
    // レビュー・口コミ関連 （登録、更新、削除）
    public static final String REVIEW_CREATE_SUCCESS = "レビューが正常に作成されました。";
    public static final String REVIEW_REGISTER_SUCCESS = "レビューが正常に登録されました。";
    public static final String REVIEW_REGISTER_FAIL = "レビューの登録中にエラーが発生しました。";
    public static final String REVIEW_WRITE_SUCCESS = "レビューが正常に投稿されました。";
    public static final String REVIEW_WRITE_FAIL = "レビューの投稿中にエラーが発生しました。";
    public static final String REVIEW_UPDATE_SUCCESS = "レビューが正常に更新されました。";
    public static final String REVIEW_UPDATE_FAIL = "レビューの更新中にエラーが発生しました。";
    public static final String REVIEW_DELETE_SUCCESS = "レビューが正常に削除されました。";
    public static final String REVIEW_DELETE_FAIL = "レビューの削除中にエラーが発生しました。";

    // レビュー・口コミの取得関連
    public static final String REVIEW_FETCH_SUCCESS = "レビュー一覧の取得に成功しました。";
    public static final String REVIEW_DETAIL_FETCH_SUCCESS = "レビュー詳細の取得に成功しました。";
    public static final String REVIEW_DETAIL_FETCH_FAIL = "レビュー詳細の取得中にエラーが発生しました。";
    public static final String REVIEW_NOT_FOUND = "レビューが見つかりません。";
    public static final String REVIEW_FETCH_EMPTY = "当該商品に対するレビューはありません。";
    public static final String REVIEW_FETCH_FAIL = "レビューの取得中にエラーが発生しました。";
    public static final String REVIEW_VIEW_ALREADY_COUNTED = "24時間以内に既に閲覧されたレビューです。閲覧数の加算をスキップします。";

    // バリデーション・権限
    public static final String REVIEW_VALIDATION_FAIL = "レビューのバリデーションに失敗しました: {}";
    public static final String REVIEW_FORBIDDEN = "レビューに対する権限がありません。";
    public static final String REVIEW_VALIDATION_ERROR_MSG = "不正なリクエストです。必須項目をご確認ください。";

    // 例外
    public static final String REVIEW_DB_ERROR = "レビュー処理中にDBエラーが発生しました: {}";
    public static final String REVIEW_UNEXPECTED_ERROR = "レビュー処理中に予期しないエラーが発生しました: {}";

    // レビュー・口コミの注文一覧関連（投稿者向け）
    public static final String REVIEW_ORDER_FETCH_SUCCESS = "注文一覧の取得に成功しました。";
    public static final String REVIEW_ORDER_FETCH_FAIL = "注文一覧の取得に失敗しました。";
    public static final String REVIEW_ORDER_GROUP_FETCH_SUCCESS = "注文一覧の取得に成功しました: memberNumber={}";
    public static final String REVIEW_ORDER_GROUP_FETCH_FAIL = "注文一覧の取得に失敗しました: memberNumber={}, error={}";

    public static final String REVIEW_ORDER_PRODUCT_FOUND = "注文内の商品存在確認に成功しました。";
    public static final String REVIEW_ORDER_PRODUCT_NOT_FOUND = "注文内の商品が見つかりません。";
    public static final String REVIEW_ORDER_PRODUCT_ERROR = "注文内の商品検証中にエラーが発生しました。";

    // レビュー・口コミの閲覧数関連
    public static final String REVIEW_VIEW_INCREMENT_SUCCESS = "レビューの閲覧数加算に成功しました。";
    public static final String REVIEW_VIEW_INCREMENT_FAIL = "レビューの閲覧数加算に失敗しました。";
    public static final String REVIEW_VIEW_INCREMENT_ERROR = "レビューの閲覧数加算中にエラーが発生しました。";
    public static final String REVIEW_VIEW_INCREMENT_ALREADY_COUNTED = "本日すでに閲覧数が反映されています。";

    // レビュー・口コミの画像関連
    public static final String REVIEW_IMAGE_UPLOAD_SUCCESS = "レビュー画像のアップロードに成功しました。";
    public static final String REVIEW_IMAGE_UPLOAD_FAIL = "レビュー画像のアップロードに失敗しました。";
    public static final String REVIEW_IMAGE_DELETE_SUCCESS = "レビュー画像の削除に成功しました。";
    public static final String REVIEW_IMAGE_DELETE_FAIL = "レビュー画像の削除に失敗しました。";
    public static final String REVIEW_IMAGE_NOT_FOUND = "レビュー画像が見つかりません。";
}
