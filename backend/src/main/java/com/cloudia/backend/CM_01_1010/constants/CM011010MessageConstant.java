package com.cloudia.backend.CM_01_1010.constants;

public final class CM011010MessageConstant {

    private CM011010MessageConstant() {
    }
    public static final String UNSUBSCRIBE_REQUEST_START = "退会処理開始: 認証コード送信リクエスト開始";
    public static final String UNSUBSCRIBE_REQUEST_END = "退会処理終了: 認証コード送信リクエスト完了";

    public static final String UNSUBSCRIBE_START = "退会処理開始: userId={}";
    public static final String UNSUBSCRIBE_END = "退会処理終了: userId={}";

    public static final String SERVICE_START = "unsubscribeサービス開始。リクエストデータ確認: userId={}";

    public static final String WARN_USER_NOT_FOUND = "退会リクエスト処理中にユーザーが見つかりません: userId={}";
    public static final String WARN_PASSWORD_MISMATCH = "退会リクエスト処理中にパスワード不一致: userId={}";
    public static final String INFO_DEACTIVATED = "ユーザーアカウントの無効化完了: userId={}";

    public static final String ERROR_DB = "退会処理中にDBエラーが発生";
    public static final String ERROR_UNEXPECTED = "退会処理中に不明なエラーが発生";

    public static final String WARN_ACTIVE_ORDERS_EXIST = "退会不可: 進行中の注文が{}件存在（ユーザー: {}）";
    public static final String FAIL_ACTIVE_ORDERS_EXIST =
            "進行中の注文または予約商品があるため退会できません。すべての取引完了後に再度お試しください。";

    public static final String SUCCESS_UNSUBSCRIBE = "退会処理が正常に完了しました。";

    public static final String FAIL_USER_NOT_FOUND = "ユーザー情報が見つかりません。";
    public static final String FAIL_PASSWORD_MISMATCH = "パスワードが一致しません。";
    public static final String FAIL_DB_ERROR = "DB処理中にエラーが発生しました。";
    public static final String FAIL_UNEXPECTED_ERROR = "サーバー内部エラーにより退会に失敗しました。";
}
