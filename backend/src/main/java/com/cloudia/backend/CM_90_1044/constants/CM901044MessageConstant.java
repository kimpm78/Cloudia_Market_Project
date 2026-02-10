package com.cloudia.backend.CM_90_1044.constants;

public class CM901044MessageConstant {
    private CM901044MessageConstant() {
    }

    // お知らせ取得関連
    public static final String NOTICE_FIND_ALL_START = "お知らせ一覧（全件）取得を開始";
    public static final String NOTICE_FIND_ALL_COMPLETE = "お知らせ一覧（全件）取得が完了しました。取得件数: {}";
    public static final String NOTICE_FIND_ALL_DB_ERROR = "お知らせ一覧（全件）取得中にDBエラーが発生しました: {}";
    public static final String NOTICE_FIND_ALL_UNEXPECTED_ERROR = "お知らせ一覧（全件）取得中に予期しないエラーが発生しました: {}";

    public static final String NOTICE_SEARCH_START = "お知らせ検索を開始。検索語: {}, 検索タイプ: {}";
    public static final String NOTICE_SEARCH_COMPLETE = "お知らせ検索が完了しました。取得件数: {}";
    public static final String NOTICE_SEARCH_FAILED_EMPTY_TERM = "お知らせ検索に失敗しました: 検索語が空です。";
    public static final String NOTICE_SEARCH_FAILED_INVALID_TYPE = "お知らせ検索に失敗しました: 無効な検索タイプです。検索タイプ: {}";
    public static final String NOTICE_SEARCH_DB_ERROR = "お知らせ検索中にDBエラーが発生しました。検索語: {}, 検索タイプ: {}, エラー: {}";
    public static final String NOTICE_SEARCH_UNEXPECTED_ERROR = "お知らせ検索中に予期しないエラーが発生しました。検索語: {}, 検索タイプ: {}, エラー: {}";

    public static final String NOTICE_FIND_BY_ID_START = "お知らせ詳細取得を開始。お知らせID: {}";
    public static final String NOTICE_FIND_BY_ID_COMPLETE = "お知らせ詳細取得が完了しました。お知らせID: {}, 取得件数: {}";
    public static final String NOTICE_FIND_BY_ID_FAILED_INVALID_ID = "お知らせ詳細取得に失敗しました: 無効なお知らせIDです。お知らせID: {}";
    public static final String NOTICE_FIND_BY_ID_DB_ERROR = "お知らせ詳細取得中にDBエラーが発生しました。お知らせID: {}, エラー: {}";
    public static final String NOTICE_FIND_BY_ID_UNEXPECTED_ERROR = "お知らせ詳細取得中に予期しないエラーが発生しました。お知らせID: {}, エラー: {}";

    // お知らせ登録 / 更新関連
    public static final String NOTICE_UPLOAD_START = "お知らせ登録を開始。タイトル: {}";
    public static final String NOTICE_UPLOAD_COMPLETE = "お知らせ登録が完了しました。タイトル: {}, 登録結果: {}";
    public static final String NOTICE_UPLOAD_DUPLICATE_KEY_ERROR = "お知らせ登録中に重複キーエラーが発生しました: {}";
    public static final String NOTICE_UPLOAD_DB_ERROR = "お知らせ登録中にDBエラーが発生しました: {}";
    public static final String NOTICE_UPLOAD_UNEXPECTED_ERROR = "お知らせ登録中に予期しないエラーが発生しました: {}";
    public static final String NOTICE_UPLOAD_FAILED_EMPTY_LIST = "お知らせ削除に失敗しました: 登録するお知らせ一覧が空です。";

    public static final String NOTICE_UPDATE_START = "お知らせ更新を開始。お知らせID: {}, タイトル: {}";
    public static final String NOTICE_UPDATE_COMPLETE = "お知らせ更新が完了しました。お知らせID: {}, 更新結果: {}";
    public static final String NOTICE_UPDATE_FAILED_NOT_EXISTS = "お知らせ更新に失敗しました: 存在しないお知らせIDです。お知らせID: {}";
    public static final String NOTICE_UPDATE_DUPLICATE_KEY_ERROR = "お知らせ更新中に重複キーエラーが発生しました: {}";
    public static final String NOTICE_UPDATE_DB_ERROR = "お知らせ更新中にDBエラーが発生しました: {}";
    public static final String NOTICE_UPDATE_UNEXPECTED_ERROR = "お知らせ更新中に予期しないエラーが発生しました: {}";
    public static final String NOTICE_UPDATE_FAILED_EMPTY_LIST = "お知らせ削除に失敗しました: 更新するお知らせ一覧が空です。";

    // 成功メッセージ
    public static final String SUCCESS_NOTICE_FIND = "お知らせの取得に成功しました";
    public static final String SUCCESS_NOTICE_UPLOAD = "お知らせの登録に成功しました";
    public static final String SUCCESS_NOTICE_UPDATE = "お知らせの更新に成功しました";

    // 失敗メッセージ
    public static final String FAIL_NO_NOTICE_SELECTED = "削除するお知らせが選択されていません。";
    public static final String FAIL_NOTICE_NOT_EXISTS = "存在しないお知らせです。";
    public static final String FAIL_SEARCH_TERM_REQUIRED = "検索語を入力してください。";
    public static final String FAIL_SEARCH_TYPE_INVALID = "無効な検索タイプです。";
    public static final String FAIL_INVALID_NOTICE_ID = "無効なお知らせIDです。";
    public static final String FAIL_DUPLICATE_NOTICE_TITLE = "同一のお知らせタイトルが既に存在します。";
    public static final String FAIL_DUPLICATE_NOTICE_INFO = "同一のお知らせ情報が既に存在します。";
    public static final String FAIL_DUPLICATE_NOTICE_UPDATE = "お知らせ情報が重複しています。";
    public static final String FAIL_NOTICE_VAL = "お知らせ登録のバリデーションに失敗しました: {}";

}