package com.cloudia.backend.constants;

public class CMMessageConstant {
    // 共通エラーメッセージ
    public static final String ERROR_DATABASE = "データベースエラーが発生しました。";
    public static final String ERROR_NULL = "必須データが不足しています。";
    public static final String ERROR_INTERNAL_SERVER = "サーバー内部エラーが発生しました。";
    public static final String ERROR_FILE_SAVE = "ファイル保存中にエラーが発生しました。";
    public static final String ERROR_FILE_UPLOAD_DIR_CREATE = "アップロードディレクトリの作成に失敗しました。";
    public static final String ERROR_FILE_SAVE_FAILED = "ファイルの保存に失敗しました。";
    public static final String FAIL_UNAUTHORIZED = "権限がありません。";

    // その他メッセージ
    public static final String MESSAGE_NULL_NOT_ALLOWED = "メッセージはnullにできません。";

    // EmailService 関連メッセージ
    public static final String EMAIL_SEND_START = "メール送信開始 - テンプレート: {}, 宛先: {}";
    public static final String EMAIL_SEND_SUCCESS = "メール送信成功 - MessageId: {}";
    public static final String EMAIL_SES_ERROR = "SESエラー - 宛先: {}, エラーコード: {}, メッセージ: {}";
    public static final String EMAIL_GENERAL_ERROR = "メール送信失敗 - 宛先: {}, エラー: {}";
    public static final String EMAIL_SES_SEND_FAILED = "SESメール送信失敗: {}";
    public static final String EMAIL_SEND_FAILED = "メール送信失敗: {}";

    // メールのバリデーションメッセージ
    public static final String EMAIL_TEMPLATE_NAME_REQUIRED = "テンプレート名は必須です。";
    public static final String EMAIL_RECIPIENT_INVALID = "正しい宛先メールアドレスではありません。";
    public static final String EMAIL_TEMPLATE_DATA_REQUIRED = "テンプレートデータは必須です。";

    // その他定数
    public static final String EMAIL_MASK_INVALID = "invalid";
}
