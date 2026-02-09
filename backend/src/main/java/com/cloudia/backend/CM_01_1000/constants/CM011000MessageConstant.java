package com.cloudia.backend.CM_01_1000.constants;

public class CM011000MessageConstant {
    private CM011000MessageConstant() {
    }
    public static final String LOGIN_REQUEST_START = "ログイン要求処理開始: {}";
    public static final String LOGIN_REQUEST_END = "ログイン要求処理完了: {}";

    public static final String AUTH_ATTEMPT = "認証試行: {}";
    public static final String AUTH_SUCCESS = "認証成功: {}";
    public static final String AUTH_FAILED_BAD_CREDENTIALS = "ログイン失敗（認証情報エラー）: ID - {}";
    public static final String AUTH_ERROR_UNEXPECTED = "ログイン中に予期しないエラーが発生: ID - {}";
    public static final String USER_NOT_FOUND_FOR_ID = "ユーザーが見つかりません: {}";
    public static final String SUCCESS_LOGIN = "ログインに成功しました。";
    public static final String ERROR_BAD_CREDENTIALS = "IDまたはパスワードが正しくありません。";
    public static final String ERROR_SERVER = "ログイン処理中にサーバーエラーが発生しました。";
    public static final String DORMANT_ACCOUNT_MESSAGE = "お客様のアカウントは休眠状態です。休眠解除手続きを行ってください。";
    public static final String INACTIVE_ACCOUNT_MESSAGE = "当該会員はご利用いただけません。";
}
