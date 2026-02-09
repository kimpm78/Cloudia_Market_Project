package com.cloudia.backend.CM_01_1001.constants;

public final class CM011001MessageConstant {

    private CM011001MessageConstant() {
        
    }

    public static final String SIGNUP_START = "会員登録開始";
    public static final String SIGNUP_END = "会員登録終了";
    public static final String SIGNUP_SERVICE_START = "signUpサービス開始。リクエストデータ確認: loginId={}";
    public static final String SIGNUP_WARN_ID_CONFLICT = "IDの重複を検出: {}";
    public static final String SIGNUP_WARN_EMAIL_CONFLICT = "メールアドレスの重複を検出: {}";
    public static final String SIGNUP_WARN_PASSWORD_MISMATCH = "パスワード不一致: {}";
    public static final String SIGNUP_WARN_EMAIL_NOT_VERIFIED = "メール未認証の試行: {}";
    public static final String SIGNUP_ERROR_DB = "会員登録中にDBエラーが発生";
    public static final String SIGNUP_ERROR_NULL = "会員登録中にNullPointerExceptionが発生";
    public static final String SIGNUP_ERROR_UNEXPECTED = "会員登録処理中に不明なエラーが発生";

    public static final String ID_CHECK_START = "ID重複チェック開始";
    public static final String ID_CHECK_END = "ID重複チェック終了";

    public static final String EMAIL_SEND_START = "メール認証コード送信開始 [email: {}]";
    public static final String EMAIL_SEND_END = "メール認証コード送信終了";
    public static final String EMAIL_SEND_SUCCESS_LOG = "メール{}へ認証コード{}を送信し、Redisに保存しました";
    public static final String EMAIL_SEND_SERVICE_ERROR = "メール送信サービス処理中に失敗: {}";
    public static final String EMAIL_SEND_DB_ERROR = "メール送信中にDBエラーが発生: {}";
    public static final String EMAIL_SEND_REDIS_ERROR = "メール送信中にRedis接続に失敗: {}";
    public static final String EMAIL_VERIFY_START = "メール認証コード検証開始 [email: {}]";
    public static final String EMAIL_VERIFY_END = "メール認証コード検証終了";
    public static final String EMAIL_VERIFY_SUCCESS_LOG = "メール認証成功 [email: {}]";
    public static final String EMAIL_VERIFY_STATUS_CLEARED_LOG = "会員登録完了後、メール{}の認証状態を削除しました。";
    public static final String EMAIL_VERIFY_WARN_NOT_EXIST_OR_EXPIRED = "認証コードが存在しない、または期限切れです [email: {}]";
    public static final String EMAIL_VERIFY_WARN_CODE_MISMATCH = "メール認証失敗: コード不一致 [email: {}, input: {}, stored: {}]";
    public static final String EMAIL_VERIFY_REDIS_ERROR = "メール認証中にRedis接続に失敗: {}";
    public static final String EMAIL_VERIFY_ERROR = "メール認証処理中に不明なエラーが発生: {}";
    public static final String EMAIL_REDIS_CHECK_ERROR = "Redisの認証状態確認中にエラーが発生: {}";
    public static final String EMAIL_REDIS_CLEAR_ERROR = "Redisの認証状態削除に失敗: {}";

    public static final String SUCCESS_SIGNUP = "会員登録が正常に完了しました。";
    public static final String SUCCESS_EMAIL_SEND = "認証コードをメールで送信しました。";
    public static final String SUCCESS_EMAIL_VERIFY = "メール認証に成功しました。";

    public static final String FAIL_ID_CONFLICT = "既に使用されているIDです。";
    public static final String FAIL_EMAIL_CONFLICT = "既に登録されているメールアドレスです。";
    public static final String FAIL_PASSWORD_MISMATCH = "パスワードが一致しません。";
    public static final String FAIL_EMAIL_NOT_VERIFIED = "メール認証が完了していません。";
    public static final String FAIL_DB_ERROR = "DB処理中にエラーが発生しました。";
    public static final String FAIL_NULL_REQUEST = "リクエストデータが不正、または必須項目が不足しています。";
    public static final String FAIL_UNEXPECTED_ERROR = "サーバー内部エラーにより会員登録に失敗しました。";

    public static final String FAIL_EMAIL_REQUIRED = "メールアドレスを入力してください。";
    public static final String FAIL_EMAIL_SEND = "メール送信に失敗しました。";
    public static final String FAIL_INVALID_VERIFICATION_CODE = "認証コードが無効、または期限切れです。";
    public static final String FAIL_CODE_MISMATCH = "認証コードが一致しません。";
    public static final String FAIL_VERIFICATION_EXPIRED = "メール認証の有効期限が切れました。再度認証を行ってください。";
    public static final String FAIL_REDIS_CONNECTION = "認証サーバーへの接続に失敗しました。";

    public static final String SES_SEND_SUCCESS = "SESメール送信成功: {} (認証コード: {})";
    public static final String SES_SEND_FAILURE = "SESメール送信失敗: {}";

    public static final String DEFAULT_ADDRESS_NICKNAME = "基本住所";
    public static final String EMAIL_VERIFICATION_SUBJECT = "会員登録メール認証";

    public static final String FAIL_KEY_GENERATION = "データベースキーの生成に失敗しました。";
    public static final String FAIL_USER_NOT_FOUND = "会員登録後のユーザー取得に失敗しました。";

    public static final String WELCOME_EMAIL_SEND_SUCCESS = "会員登録ウェルカムメール送信成功: {}";
    public static final String WELCOME_EMAIL_SEND_FAIL = "会員登録ウェルカムメール送信失敗: {}";
}