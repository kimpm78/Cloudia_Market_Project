package com.cloudia.backend.CM_02_1000.constants;

public class CM021000MessageConstant {
    private CM021000MessageConstant() {
    }

    // ヘッダーメニュー取得関連
    public static final String HEADER_MENU_START = "ヘッダーメニュー情報の取得開始";
    public static final String HEADER_MENU_END = "ヘッダーメニュー情報の取得完了";
    public static final String HEADER_ICON_START = "ヘッダーアイコン情報の取得開始";
    public static final String HEADER_ICON_END = "ヘッダーアイコン情報の取得完了";
    public static final String HEADER_MENU_COUNT = "ヘッダーメニュー数: {}";
    public static final String HEADER_MENU_DB_ERROR = "DBアクセス中にエラーが発生しました（ヘッダーメニュー）: {}";
    public static final String HEADER_MENU_NULL_ERROR = "NullPointerException が発生しました（ヘッダーメニュー）: {}";
    public static final String HEADER_MENU_UNKNOWN_ERROR = "予期しないエラーが発生しました（ヘッダーメニュー）: {}";
    
    // ヘッダーアイコン取得関連
    public static final String HEADER_ICON_COUNT = "アイコンメニュー数: {}";
    public static final String HEADER_ICON_DB_ERROR = "DBアクセス中にエラーが発生しました（アイコンメニュー）: {}";
    public static final String HEADER_ICON_NULL_ERROR = "NullPointerException が発生しました（アイコンメニュー）: {}";
    public static final String HEADER_ICON_UNKNOWN_ERROR = "予期しないエラーが発生しました（アイコンメニュー）: {}";

    // カート関連
    public static final String CART_ADD_START = "カート追加処理開始";
    public static final String CART_ADD_SUCCESS = "カート追加完了";
    public static final String CART_ADD_FAILED = "カート追加失敗";
    public static final String CART_ADD_RESPONSE = "カートに商品が追加されました。";
    public static final String CART_LOGIN_REQUIRED = "ログインが必要です。";
    public static final String CART_GET_SUCCESS = "カート取得完了: {}件、合計 {}円";

    // バナー取得関連
    public static final String BANNER_FIND_ALL_START = "バナー一覧取得開始";
    public static final String BANNER_FIND_ALL_COMPLETE = "バナー一覧取得完了、取得件数: {}";
    public static final String BANNER_FIND_ALL_DB_ERROR = "バナー一覧取得中にDBエラーが発生しました: {}";
    public static final String BANNER_FIND_ALL_UNEXPECTED_ERROR = "バナー一覧取得中に予期しないエラーが発生しました: {}";

    public static final String BANNER_SEARCH_START = "バナー検索開始、検索語: {}";
    public static final String BANNER_SEARCH_COMPLETE = "バナー検索完了、取得件数: {}";
    public static final String BANNER_SEARCH_FAILED_EMPTY_TERM = "バナー検索失敗: 検索語が空です。";
    public static final String BANNER_SEARCH_DB_ERROR = "バナー検索中にDBエラーが発生しました。検索語: {}, エラー: {}";
    public static final String BANNER_SEARCH_UNEXPECTED_ERROR = "バナー検索中に予期しないエラーが発生しました。検索語: {}, エラー: {}";

    public static final String BANNER_FIND_BY_ID_START = "バナー詳細取得開始、バナーID: {}";
    public static final String BANNER_FIND_BY_ID_COMPLETE = "バナー詳細取得完了、バナーID: {}";
    public static final String BANNER_FIND_BY_ID_FAILED_INVALID_ID = "バナー詳細取得失敗: 無効なバナーIDです。バナーID: {}";
    public static final String BANNER_FIND_BY_ID_DB_ERROR = "バナー詳細取得中にDBエラーが発生しました。バナーID: {}, エラー: {}";
    public static final String BANNER_FIND_BY_ID_UNEXPECTED_ERROR = "バナー詳細取得中に予期しないエラーが発生しました。バナーID: {}, エラー: {}";

    // ========================================
    // レスポンスメッセージ
    // ========================================

    // 成功メッセージ
    public static final String SUCCESS_BANNER_FIND = "バナー取得成功";
}