package com.cloudia.backend.CM_01_1003.constants;

public class CM011003MessageConstant {
    private CM011003MessageConstant() {
    }

    public static final String SEND_CODE_REQUEST_START = "パスワード再設定: 認証コード送信リクエスト開始";
    public static final String SEND_CODE_REQUEST_END = "パスワード再設定: 認証コード送信リクエスト完了";
    public static final String VERIFY_CODE_REQUEST_START = "パスワード再設定: コード検証リクエスト開始";
    public static final String VERIFY_CODE_REQUEST_END = "パスワード再設定: コード検証リクエスト完了";
    public static final String RESET_PASSWORD_REQUEST_START = "パスワード再設定リクエスト開始";
    public static final String RESET_PASSWORD_REQUEST_END = "パスワード再設定リクエスト完了";

    public static final String EMAIL_SEND_SUCCESS_LOG = "パスワード再設定のため、{} に認証コード {} を送信しました";
    public static final String VERIFY_SUCCESS_LOG = "パスワード再設定の認証に成功: email={}";
    public static final String RESET_PASSWORD_SUCCESS_LOG = "パスワード再設定に成功: email={}";

    public static final String EMAIL_NOT_REGISTERED_LOG = "パスワード再設定失敗: 未登録のメールアドレス (email: {})";
    public static final String EMAIL_SEND_ERROR_LOG = "パスワード再設定メール送信サービス失敗: {}";
    public static final String VERIFY_EXPIRED_LOG = "パスワード再設定の検証失敗: Redisにコードが存在しない、または期限切れ (email: {})";
    public static final String VERIFY_MISMATCH_LOG = "パスワード再設定の検証失敗: コード不一致 (email: {}, input: {}, stored: {})";
    public static final String VERIFICATION_REQUIRED_LOG = "パスワード再設定失敗: メール認証が先に完了していません (email: {})";
    public static final String USER_NOT_FOUND_LOG = "パスワード再設定失敗: ユーザーが見つかりません (email: {})";
    public static final String RESET_PASSWORD_ERROR_UNEXPECTED = "パスワード再設定中に予期しないエラーが発生: {}";
    public static final String PASSWORD_REUSE_LOG = "パスワード再利用の試行（6ヶ月以内）: email={}";
    public static final String ADD_RESET_PASSWORD_HISTORY = "パスワード再設定履歴を追加しました: memberNumber={}";

    public static final String EMAIL_NOT_REGISTERED = "登録されていないメールアドレスです。";
    public static final String EMAIL_SEND_FAILED = "メールの送信に失敗しました。";
    public static final String EMAIL_SEND_SUCCESS = "認証コードをメールで送信しました。";
    public static final String VERIFICATION_CODE_INVALID = "認証コードが正しくない、または期限切れです。";
    public static final String VERIFICATION_SUCCESS = "認証に成功しました。";
    public static final String VERIFICATION_REQUIRED = "先にメール認証を行ってください。";
    public static final String USER_NOT_FOUND = "ユーザー情報の取得中にエラーが発生しました。";
    public static final String PASSWORD_RESET_SUCCESS = "パスワードを正常に変更しました。";
    public static final String PASSWORD_RESET_FAILED = "パスワードの変更に失敗しました。";
    public static final String PASSWORD_REUSE_WITHIN_6_MONTHS = "過去6ヶ月以内に使用したパスワードは再利用できません。";
}