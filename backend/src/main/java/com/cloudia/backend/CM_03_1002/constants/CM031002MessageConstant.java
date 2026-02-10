package com.cloudia.backend.CM_03_1002.constants;

public class CM031002MessageConstant {
    private CM031002MessageConstant() {
    }
    // 商品 照会
    public static final String PRODUCT_FIND_ALL_START = "商品一覧取得開始";
    public static final String PRODUCT_FIND_ALL_COMPLETE = "商品一覧取得完了、取得件数: {}";
    public static final String PRODUCT_FIND_ALL_DB_ERROR = "商品一覧取得中にDBエラー発生: {}";
    public static final String PRODUCT_FIND_ALL_UNEXPECTED_ERROR = "商品一覧取得中に予期しないエラー発生: {}";

    public static final String PRODUCT_SEARCH_START = "商品検索開始、検索語: {}、検索タイプ: {}";
    public static final String PRODUCT_SEARCH_COMPLETE = "商品検索完了、取得件数: {}";
    public static final String PRODUCT_SEARCH_FAILED_EMPTY_TERM = "商品検索失敗: 検索語が空です。";
    public static final String PRODUCT_SEARCH_FAILED_INVALID_TYPE = "商品検索失敗: 無効な検索タイプです。検索タイプ: {}";
    public static final String PRODUCT_SEARCH_DB_ERROR = "商品検索中にDBエラー発生、検索語: {}、検索タイプ: {}、エラー: {}";
    public static final String PRODUCT_SEARCH_UNEXPECTED_ERROR = "商品検索中に予期しないエラー発生、検索語: {}、検索タイプ: {}、エラー: {}";

    public static final String PRODUCT_FIND_BY_ID_START = "商品詳細取得開始、商品ID: {}";
    public static final String PRODUCT_FIND_BY_ID_COMPLETE = "商品詳細取得完了、商品ID: {}、取得件数: {}";
    public static final String PRODUCT_FIND_BY_ID_FAILED_INVALID_ID = "商品詳細取得失敗: 無効な商品IDです。商品ID: {}";
    public static final String PRODUCT_FIND_BY_ID_DB_ERROR = "商品詳細取得中にDBエラー発生、商品ID: {}、エラー: {}";
    public static final String PRODUCT_FIND_BY_ID_UNEXPECTED_ERROR = "商品詳細取得中に予期しないエラー発生、商品ID: {}、エラー: {}";

    // カテゴリー 照会
    public static final String CATEGORY_GROUP_FETCH_START = "カテゴリーグループ取得開始";
    public static final String CATEGORY_GROUP_FETCH_SUCCESS = "カテゴリーグループ取得成功";
    public static final String CATEGORY_GROUP_FETCH_DB_ERROR = "カテゴリーグループ取得中にDBエラー発生: {}";
    public static final String CATEGORY_GROUP_FETCH_NULL = "カテゴリーグループ取得失敗: 結果がnullです。";
    public static final String CATEGORY_GROUP_FETCH_ERROR = "カテゴリーグループ取得中に予期しないエラー発生: {}";

    public static final String CATEGORY_DETAIL_FETCH_START = "下位カテゴリー取得開始、グループコード: {}";
    public static final String CATEGORY_DETAIL_FETCH_SUCCESS = "下位カテゴリー情報取得成功";
    public static final String CATEGORY_DETAIL_FETCH_DB_ERROR = "下位カテゴリー取得中にDBエラー発生: {}";
    public static final String CATEGORY_DETAIL_FETCH_NULL = "下位カテゴリー取得失敗: 結果がnullです。";
    public static final String CATEGORY_DETAIL_FETCH_ERROR = "下位カテゴリー取得中に予期しないエラー発生: {}";

    // 商品 登録 / 更新
    public static final String PRODUCT_UPLOAD_START = "商品登録開始、商品名: {}";
    public static final String PRODUCT_UPLOAD_COMPLETE = "商品登録完了、商品名: {}、登録結果: {}";
    public static final String PRODUCT_UPLOAD_DB_ERROR = "商品登録中にDBエラー発生: {}";
    public static final String PRODUCT_UPLOAD_UNEXPECTED_ERROR = "商品登録中に予期しないエラー発生: {}";
    public static final String PRODUCT_UPLOAD_FAILED_EMPTY_LIST = "商品登録失敗: 登録対象の商品一覧が空です。";

    public static final String PRODUCT_UPDATE_START = "商品更新開始、商品ID: {}、商品名: {}";
    public static final String PRODUCT_UPDATE_COMPLETE = "商品更新完了、商品ID: {}、更新結果: {}";
    public static final String PRODUCT_UPDATE_FAILED_NOT_EXISTS = "商品更新失敗: 存在しない商品IDです。商品ID: {}";
    public static final String PRODUCT_UPDATE_DB_ERROR = "商品更新中にDBエラー発生: {}";
    public static final String PRODUCT_UPDATE_UNEXPECTED_ERROR = "商品更新中に予期しないエラー発生: {}";
    public static final String PRODUCT_UPDATE_FAILED_EMPTY_LIST = "商品更新失敗: 更新対象の商品一覧が空です。";

    // 商品 削除
    public static final String PRODUCT_DELETE_START = "商品削除開始、商品ID: {}";
    public static final String PRODUCT_DELETE_COMPLETE = "商品削除完了、商品ID: {}、削除結果: {}";
    public static final String PRODUCT_DELETE_FAILED_INVALID_ID = "商品削除失敗: 無効な商品IDです。商品ID: {}";
    public static final String PRODUCT_DELETE_FAILED_NOT_EXISTS = "商品削除失敗: 存在しない商品です。商品ID: {}";
    public static final String PRODUCT_DELETE_DB_ERROR = "商品削除中にDBエラー発生、商品ID: {}、エラー: {}";
    public static final String PRODUCT_DELETE_UNEXPECTED_ERROR = "商品削除中に予期しないエラー発生、商品ID: {}、エラー: {}";

    // 成功・失敗メッセージ
    public static final String SUCCESS_PRODUCT_FIND = "商品取得成功";
    public static final String SUCCESS_PRODUCT_UPLOAD = "商品登録成功";
    public static final String SUCCESS_PRODUCT_UPDATE = "商品更新成功";
    public static final String SUCCESS_PRODUCT_DELETE = "商品削除成功";

    public static final String FAIL_PRODUCT_NOT_SELECTED = "削除対象の商品が選択されていません。";
    public static final String FAIL_PRODUCT_NOT_EXISTS = "存在しない商品です。";
    public static final String FAIL_PRODUCT_SEARCH_TERM_REQUIRED = "検索語を入力してください。";
    public static final String FAIL_PRODUCT_SEARCH_TYPE_INVALID = "無効な検索タイプです。";
    public static final String FAIL_PRODUCT_INVALID_ID = "無効な商品IDです。";
    public static final String FAIL_PRODUCT_DUPLICATE_NAME = "既に存在する商品名です。";
    public static final String FAIL_PRODUCT_VALIDATION = "商品登録バリデーション失敗: {}";

    public static final String FAIL_PRODUCT_LIST_EMPTY = "商品一覧が存在しません。";
    public static final String FAIL_PRODUCT_LIST_FETCH_NULL = "商品一覧取得失敗: レスポンスまたは本文がnullです。";

    // カート関連
    public static final String SUCCESS_CART_ADD = "カートに商品を追加しました。";
    public static final String SUCCESS_CART_UPDATE = "カート数量を変更しました。";
    public static final String FAIL_CART_ADD = "カート追加失敗: エラーが発生しました。";
    public static final String FAIL_CART_UPDATE = "カート数量変更失敗: エラーが発生しました。";

    // ファイル関連
    public static final String FILE_UPLOAD_PATH_DEBUG = "ファイルアップロードパス: {}";
    public static final String FILE_UPLOAD_DIR_CREATED = "アップロードディレクトリ作成: {}";
    public static final String FILE_UPLOAD_DIR_CREATE_FAILED = "アップロードディレクトリ作成失敗: {}";
    public static final String FILE_SAVE_COMPLETE = "ファイル保存完了: {}";
    public static final String FILE_SAVE_FAILED = "ファイル保存失敗: {}";
    public static final String FILE_DELETE_FAILED_CLEANUP = "失敗したファイル削除中にエラー発生: {}";

    public static final String IMAGE_DELETE_NO_LINK = "削除対象の画像リンクがありません。";
    public static final String IMAGE_DELETE_COMPLETE = "画像ファイル削除完了: {}";
    public static final String IMAGE_DELETE_NOT_EXISTS = "削除対象の画像ファイルが存在しません: {}";
    public static final String IMAGE_DELETE_FILE_NOT_FOUND = "削除対象のファイルが存在しません: {}";
    public static final String IMAGE_DELETE_FAILED = "画像ファイル削除失敗: {}、エラー: {}";
    public static final String IMAGE_DELETE_UNEXPECTED_ERROR = "画像ファイル削除中に予期しないエラー発生: {}、エラー: {}";
    
    // ========================================
    // 応答メッセージ
    // ========================================

    // 成功メッセージ
    public static final String SUCCESS_BANNER_DELETE = "バナー削除成功";
    public static final String SUCCESS_BANNER_FIND = "バナー取得成功";
    public static final String SUCCESS_BANNER_UPLOAD = "バナー登録成功";
    public static final String SUCCESS_BANNER_UPDATE = "バナー更新成功";
    public static final String SUCCESS_DISPLAY_ORDER_FIND = "ディスプレイ番号取得成功";

    // 失敗メッセージ
    public static final String FAIL_NO_BANNER_SELECTED = "削除対象のバナーが選択されていません。";
    public static final String FAIL_REFERENCED_BANNER = "他データで参照中のバナーは削除できません。";
    public static final String FAIL_BANNER_NOT_FOUND = "削除対象のバナーが見つかりません。";
    public static final String FAIL_BANNER_NOT_EXISTS = "存在しないバナーです。";
    public static final String FAIL_SEARCH_TERM_REQUIRED = "検索語を入力してください。";
    public static final String FAIL_INVALID_BANNER_ID = "無効なバナーIDです。";
    public static final String FAIL_MAX_ACTIVE_BANNERS = "有効なバナー数が最大10件を超えました。";
    public static final String FAIL_DUPLICATE_DISPLAY_ORDER = "バナー表示順が重複しています。";
    public static final String FAIL_INVALID_FILE_TYPE = "許可されていないファイル形式です。";
    public static final String FAIL_DUPLICATE_BANNER_INFO = "既に存在するバナー情報です。";
    public static final String FAIL_DUPLICATE_BANNER_UPDATE = "重複したバナー情報です。";
    public static final String FAIL_BANNER_VAL = "バナー登録バリデーション失敗: {}";
    public static final String FAIL_BANNER_UPDATE = "当該バナーは別のユーザーにより更新されました。再取得してから更新してください。";
}