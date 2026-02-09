package com.cloudia.backend.CM_01_1002.constants;

public class CM011002MesaageConstant {
    private CM011002MesaageConstant() {
    }
    public static final String FIND_ID_CODE_SEND_START = "ID検索コード送信リクエスト受信開始";
    public static final String FIND_ID_CODE_SEND_END = "ID検索コード送信リクエスト受信完了";
    public static final String FIND_ID_VERIFY_START = "ID検索コード検証リクエスト受信開始";
    public static final String FIND_ID_VERIFY_END = "ID検索コード検証リクエスト受信完了";

    public static final String FIND_ID_INVALID_EMAIL_FORMAT_LOG = "ID検索失敗: 無効なメールアドレス形式です。({})";
    public static final String FIND_ID_TOO_MANY_REQUESTS_LOG = "ID検索失敗: リクエストが多すぎます。({})";
    public static final String FIND_ID_EMAIL_NOT_REGISTERED_LOG = "ID検索失敗: 登録されていないメールアドレスです。({})";
    public static final String FIND_ID_EMAIL_SEND_SUCCESS_LOG = "ID検索のため、{} に認証コード {} を送信しました";
    public static final String FIND_ID_EMAIL_SEND_SERVICE_ERROR_LOG = "ID検索メール送信サービス処理中に失敗: {}";
    public static final String FIND_ID_VERIFY_FAILED_RESPONSE_LOG = "ID検索検証失敗: 認証サービスの応答ステータスコード {} または verified=false";
    public static final String FIND_ID_USER_NOT_FOUND_FATAL_ERROR_LOG = "致命的エラー: 認証は成功したがユーザーが見つかりません。email: {}";
    public static final String FIND_ID_SUCCESS_LOG = "ID検索成功。Email: {}, Found LoginId: {}";
    public static final String FIND_ID_VERIFY_MISMATCH_LOG = "ID検索検証失敗: コードが一致しません。email: {}, input: {}, stored: {}";
    public static final String FIND_ID_VERIFY_EXPIRED_LOG = "ID検索検証失敗: Redisにコードが存在しない、または期限切れです。email: {}";

    public static final String FIND_ID_INVALID_EMAIL_FORMAT = "メールアドレスの形式が正しくありません。";
    public static final String FIND_ID_TOO_MANY_REQUESTS = "1分後にもう一度お試しください。";
    public static final String FIND_ID_EMAIL_NOT_REGISTERED = "登録されていないメールアドレスです。";
    public static final String FIND_ID_EMAIL_SEND_FAILED = "メール送信に失敗しました。";
    public static final String FIND_ID_USER_NOT_FOUND_AFTER_VERIFY = "ユーザー情報の取得に失敗しました。";

    public static final String FIND_ID_EMAIL_SEND_SUCCESS = "認証コードをメールで送信しました。";
    public static final String FIND_ID_VERIFY_MISMATCH = "認証番号が一致しません。";
    public static final String FIND_ID_VERIFY_EXPIRED = "認証時間が期限切れ、またはリクエスト履歴がありません。";
}
