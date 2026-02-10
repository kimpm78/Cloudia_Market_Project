package com.cloudia.backend.CM_90_1051.constants;

public class CM901051MessageConstant {
    // 参照関連メッセージ
    public static final String ORDER_FIND_ALL_START = "注文一覧取得を開始します。";
    public static final String ORDER_FIND_ALL_COMPLETE = "注文一覧取得が完了しました。取得件数: {} 件";
    public static final String ORDER_FIND_ALL_DB_ERROR = "注文一覧取得中にデータベースエラーが発生しました。エラー: {}";
    public static final String ORDER_FIND_ALL_NULL_ERROR = "注文一覧取得中にNULLエラーが発生しました。エラー: {}";
    public static final String ORDER_FIND_ALL_UNEXPECTED_ERROR = "注文一覧取得中に予期しないエラーが発生しました。エラー: {}";

    public static final String ORDER_SEARCH_START = "注文検索を開始します。";
    public static final String ORDER_SEARCH_COMPLETE = "注文検索が完了しました。取得件数: {} 件";
    public static final String ORDER_SEARCH_DB_ERROR = "注文検索中にデータベースエラーが発生しました。エラー: {}";
    public static final String ORDER_SEARCH_NULL_ERROR = "注文検索中にNULLエラーが発生しました。エラー: {}";
    public static final String ORDER_SEARCH_UNEXPECTED_ERROR = "注文検索中に予期しないエラーが発生しました。エラー: {}";

    public static final String ORDER_DETAIL_FIND_START = "注文詳細取得を開始します。";
    public static final String ORDER_DETAIL_FIND_COMPLETE = "注文詳細取得が完了しました。取得件数: {} 件";
    public static final String ORDER_DETAIL_FIND_DB_ERROR = "注文詳細取得中にデータベースエラーが発生しました。エラー: {}";
    public static final String ORDER_DETAIL_FIND_NULL_ERROR = "注文詳細取得中にNULLエラーが発生しました。エラー: {}";
    public static final String ORDER_DETAIL_FIND_UNEXPECTED_ERROR = "注文詳細取得中に予期しないエラーが発生しました。エラー: {}";

    // 更新関連メッセージ
    public static final String ORDER_UPDATE_START = "更新を開始します。";
    public static final String ORDER_UPDATE_EMPTY_REQUEST = "更新リクエストデータが空です。";
    public static final String ORDER_UPDATE_NOT_FOUND = "更新対象の注文が見つかりません。";
    public static final String ORDER_UPDATE_DUPLICATE_ERROR = "注文更新中に重複キーエラーが発生しました。エラー: {}";
    public static final String ORDER_UPDATE_INTEGRITY_ERROR = "注文更新中にデータ整合性違反が発生しました。エラー: {}";
    public static final String ORDER_UPDATE_DB_ERROR = "注文更新中にデータベースエラーが発生しました。エラー: {}";
    public static final String ORDER_UPDATE_UNEXPECTED_ERROR = "注文更新中に予期しないエラーが発生しました。エラー: {}";

    // 成功メッセージ
    public static final String SUCCESS_ORDER_FIND = "注文取得が正常に完了しました。";
    public static final String SUCCESS_ORDER_SEARCH = "注文検索が正常に完了しました。";
    public static final String SUCCESS_ORDER_DETAIL_FIND = "注文詳細取得が正常に完了しました。";
    public static final String SUCCESS_ORDER_UPDATE = "注文ステータスが正常に更新されました。";

    // メール送信関連メッセージ
    public static final String EMAIL_START = "メール送信を開始します。";
    public static final String EMAIL_VALIDATION = "メール検証を開始します。";
    public static final String EMAIL_CUSTOMER_INFO_NOT_FOUND = "顧客のメール情報がないため、発送通知を送信できません。";
    public static final String EMAIL_ORDER_INFO_NOT_FOUND = "注文情報が見つからないため、発送通知を送信できません。";
    public static final String EMAIL_SEND_SUCCESS = "発送開始メール送信成功 - 注文番号: {}";
    public static final String EMAIL_SEND_INPUT_ERROR = "メール送信入力値エラー - 注文番号: {}, エラー: {}";
    public static final String EMAIL_SEND_SYSTEM_ERROR = "メール送信システムエラー - 注文番号: {}, エラー: {}";
    public static final String EMAIL_SEND_GENERAL_ERROR = "発送開始メール送信失敗 - 注文番号: {}, エラー: {}";
    public static final String EMAIL_SEND_FAILED_INPUT = "発送通知送信失敗: {}";
    public static final String EMAIL_SEND_FAILED_SYSTEM = "発送通知送信失敗: システムエラーが発生しました。";
    public static final String EMAIL_SEND_FAILED_GENERAL = "発送通知メールの送信に失敗しました。";
    public static final String EMAIL_ORDER_INFO_QUERY_ERROR = "顧客メール情報なし - 注文番号: {}";
    public static final String EMAIL_ORDER_QUERY_FAILED = "注文情報取得失敗 - 注文番号: {}";
}
