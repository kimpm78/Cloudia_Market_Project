package com.cloudia.backend.CM_90_1040.constants;

public class CM901040MessageConstant {
    private CM901040MessageConstant() {
    }

    // バナー削除関連
    public static final String BANNER_DELETE_START = "バナー削除開始、削除対象件数: {}";
    public static final String BANNER_DELETE_COMPLETE = "バナー削除完了、ID: {}、画像パス: {}";
    public static final String BANNER_DELETE_SUCCESS = "バナー削除完了、削除件数: {}";
    public static final String BANNER_DELETE_FAILED_EMPTY_LIST = "バナー削除失敗: 削除するバナー一覧が空です。";
    public static final String BANNER_DELETE_FAILED_INVALID_INFO = "無効なバナー情報のためスキップします: {}";
    public static final String BANNER_DELETE_FAILED_INTEGRITY_VIOLATION = "バナー削除失敗 - 参照整合性制約違反、ID: {}";
    public static final String BANNER_DELETE_FAILED_NO_RESULT = "バナー削除結果: 削除されたバナーがありません。";
    public static final String BANNER_DELETE_DB_ERROR = "バナー削除中にDBエラーが発生しました: {}";
    public static final String BANNER_DELETE_UNEXPECTED_ERROR = "バナー削除中に予期しないエラーが発生しました: {}";

    // バナー取得関連
    public static final String BANNER_FIND_ALL_START = "バナー全件一覧取得開始";
    public static final String BANNER_FIND_ALL_COMPLETE = "バナー全件一覧取得完了、取得件数: {}";
    public static final String BANNER_FIND_ALL_DB_ERROR = "バナー全件一覧取得中にDBエラーが発生しました: {}";
    public static final String BANNER_FIND_ALL_UNEXPECTED_ERROR = "バナー全件一覧取得中に予期しないエラーが発生しました: {}";

    public static final String BANNER_SEARCH_START = "バナー検索開始、検索キーワード: {}";
    public static final String BANNER_SEARCH_COMPLETE = "バナー検索完了、取得件数: {}";
    public static final String BANNER_SEARCH_FAILED_EMPTY_TERM = "バナー検索失敗: 検索キーワードが空です。";
    public static final String BANNER_SEARCH_DB_ERROR = "バナー検索中にDBエラーが発生しました、検索キーワード: {}、エラー: {}";
    public static final String BANNER_SEARCH_UNEXPECTED_ERROR = "バナー検索中に予期しないエラーが発生しました、検索キーワード: {}、エラー: {}";

    public static final String BANNER_FIND_BY_ID_START = "バナー詳細取得開始、バナーID: {}";
    public static final String BANNER_FIND_BY_ID_COMPLETE = "バナー詳細取得完了、バナーID: {}";
    public static final String BANNER_FIND_BY_ID_FAILED_INVALID_ID = "バナー詳細取得失敗: 無効なバナーIDです。バナーID: {}";
    public static final String BANNER_FIND_BY_ID_DB_ERROR = "バナー詳細取得中にDBエラーが発生しました、バナーID: {}、エラー: {}";
    public static final String BANNER_FIND_BY_ID_UNEXPECTED_ERROR = "バナー詳細取得中に予期しないエラーが発生しました、バナーID: {}、エラー: {}";

    // バナー登録・更新関連
    public static final String BANNER_UPLOAD_START = "バナー登録開始、バナー名: {}";
    public static final String BANNER_UPLOAD_COMPLETE = "バナー登録完了、バナー名: {}、登録結果: {}";
    public static final String BANNER_UPLOAD_ACTIVE_COUNT_CHECK = "現在の有効バナー数: {}";
    public static final String BANNER_UPLOAD_FAILED_MAX_EXCEEDED = "バナー登録失敗: 有効バナー数が上限（{}）を超えています。現在数: {}";
    public static final String BANNER_UPLOAD_FAILED_DUPLICATE_ORDER = "バナー登録失敗: 表示順が重複しています。順序: {}";
    public static final String BANNER_FILE_SAVED = "バナー画像ファイルの保存完了: {}";
    public static final String BANNER_UPLOAD_SECURITY_ERROR = "バナー登録中にセキュリティエラーが発生しました: {}";
    public static final String BANNER_UPLOAD_FILE_ERROR = "バナー登録中にファイル保存エラーが発生しました: {}";
    public static final String BANNER_UPLOAD_DUPLICATE_KEY_ERROR = "バナー登録中に重複キーエラーが発生しました: {}";
    public static final String BANNER_UPLOAD_DB_ERROR = "バナー登録中にDBエラーが発生しました: {}";
    public static final String BANNER_UPLOAD_UNEXPECTED_ERROR = "バナー登録中に予期しないエラーが発生しました: {}";
    public static final String BANNER_UPDATE_START = "バナー更新開始、バナーID: {}、バナー名: {}";
    public static final String BANNER_UPDATE_COMPLETE = "バナー更新完了、バナーID: {}、更新結果: {}";
    public static final String BANNER_UPDATE_FAILED_NOT_EXISTS = "バナー更新失敗: 存在しないバナーIDです。バナーID: {}";
    public static final String BANNER_UPDATE_FAILED_MAX_EXCEEDED = "バナー更新失敗: 有効バナー数が上限（{}）を超えています。現在数: {}";
    public static final String BANNER_UPDATE_FAILED_DUPLICATE_ORDER = "バナー更新失敗: 表示順が重複しています。順序: {}";
    public static final String BANNER_UPDATE_SECURITY_ERROR = "バナー更新中にセキュリティエラーが発生しました: {}";
    public static final String BANNER_UPDATE_FILE_ERROR = "バナー更新中にファイル保存エラーが発生しました: {}";
    public static final String BANNER_UPDATE_DUPLICATE_KEY_ERROR = "バナー更新中に重複キーエラーが発生しました: {}";
    public static final String BANNER_UPDATE_DB_ERROR = "バナー更新中にDBエラーが発生しました: {}";
    public static final String BANNER_UPDATE_UNEXPECTED_ERROR = "バナー更新中に予期しないエラーが発生しました: {}";

    // ディスプレイ順序関連
    public static final String DISPLAY_ORDER_FIND_START = "使用可能なディスプレイ番号照会開始";
    public static final String DISPLAY_ORDER_FIND_COMPLETE = "使用可能なディスプレイ番号照会完了、照会された番号数: {}";
    public static final String DISPLAY_ORDER_FIND_DB_ERROR = "ディスプレイ番号照会中にDBエラーが発生しました: {}";
    public static final String DISPLAY_ORDER_FIND_UNEXPECTED_ERROR = "ディスプレイ番号照会中に予期しないエラーが発生しました: {}";
    public static final String DISPLAY_ORDER_DUPLICATE_CHECK_ERROR = "ディスプレイ順序重複確認中にエラーが発生しました: {}";

    // ファイル操作関連
    public static final String FILE_UPLOAD_PATH_DEBUG = "ファイルアップロードパス: {}";
    public static final String FILE_UPLOAD_DIR_CREATED = "アップロードディレクトリ作成: {}";
    public static final String FILE_UPLOAD_DIR_CREATE_FAILED = "アップロードディレクトリ作成失敗: {}";
    public static final String FILE_SAVE_COMPLETE = "ファイル保存完了: {}";
    public static final String FILE_SAVE_FAILED = "ファイル保存失敗: {}";
    public static final String FILE_DELETE_FAILED_CLEANUP = "失敗したファイル削除中にエラーが発生しました: {}";

    public static final String IMAGE_DELETE_NO_LINK = "削除する画像リンクがありません。";
    public static final String IMAGE_DELETE_COMPLETE = "画像ファイル削除完了: {}";
    public static final String IMAGE_DELETE_NOT_EXISTS = "削除する画像ファイルが存在しません: {}";
    public static final String IMAGE_DELETE_FILE_NOT_FOUND = "削除するファイルが存在しません: {}";
    public static final String IMAGE_DELETE_FAILED = "画像ファイル削除失敗: {}, エラー: {}";
    public static final String IMAGE_DELETE_UNEXPECTED_ERROR = "画像ファイル削除中に予期しないエラーが発生しました: {}, エラー: {}";

    // その他
    public static final String ACTIVE_BANNER_COUNT_ERROR = "有効バナー数の取得中にエラーが発生しました: {}";

    // ========================================
    // 응답 메시지
    // ========================================

    // 成功メッセージ
    public static final String SUCCESS_BANNER_DELETE = "バナー削除成功";
    public static final String SUCCESS_BANNER_FIND = "バナー照会成功";
    public static final String SUCCESS_BANNER_UPLOAD = "バナー登録成功";
    public static final String SUCCESS_BANNER_UPDATE = "バナー更新成功";
    public static final String SUCCESS_DISPLAY_ORDER_FIND = "ディスプレイ番号照会成功";
    // 失敗メッセージ
    public static final String FAIL_NO_BANNER_SELECTED = "削除するバナーが選択されていません。";
    public static final String FAIL_REFERENCED_BANNER = "他のデータから参照されているバナーは削除できません。";
    public static final String FAIL_BANNER_NOT_FOUND = "削除するバナーが見つかりません。";
    public static final String FAIL_BANNER_NOT_EXISTS = "存在しないバナーです。";
    public static final String FAIL_SEARCH_TERM_REQUIRED = "検索語を入力してください。";
    public static final String FAIL_INVALID_BANNER_ID = "無効なバナーIDです。";
    public static final String FAIL_MAX_ACTIVE_BANNERS = "有効なバナー数が最大8個を超えています。";
    public static final String FAIL_DUPLICATE_DISPLAY_ORDER = "バナー順序が重複しています。";
    public static final String FAIL_INVALID_FILE_TYPE = "許可されていないファイル形式です。";
    public static final String FAIL_DUPLICATE_BANNER_INFO = "既に存在するバナー情報です。";
    public static final String FAIL_DUPLICATE_BANNER_UPDATE = "重複されたバナー情報です。";
    public static final String FAIL_BANNER_VAL = "バナー登録検証失敗: {}";
    public static final String FAIL_BANNER_UPDATE = "該当バナーは他のユーザーが更新しました。再度照会後、更新してください。";
}
