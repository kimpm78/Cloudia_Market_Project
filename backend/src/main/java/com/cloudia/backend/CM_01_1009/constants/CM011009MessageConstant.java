package com.cloudia.backend.CM_01_1009.constants;

public final class CM011009MessageConstant {

    private CM011009MessageConstant() {
    }

    public static final String CHANGE_PASSWORD_REQUEST_START = "パスワード変更リクエスト開始";
    public static final String CHANGE_PASSWORD_REQUEST_END = "パスワード変更リクエスト完了";

    public static final String SERVICE_START = "パスワード変更サービス開始: loginId={}";
    public static final String SERVICE_END = "パスワード変更完了: loginId={}";

    public static final String UNEXPECTED_ERROR = "パスワード変更処理中に例外が発生";

    public static final String SUCCESS_CHANGE_PASSWORD = "パスワードが正常に変更されました。";

    public static final String FAIL_PASSWORD_CONFIRM_MISMATCH = "新しいパスワードが一致しません。";
    public static final String FAIL_USER_NOT_FOUND = "ユーザー情報が見つかりません。";
    public static final String FAIL_CURRENT_PASSWORD_MISMATCH = "現在のパスワードが正しくありません。";
    public static final String FAIL_NEW_PASSWORD_SAME_AS_OLD = "新しいパスワードは現在のパスワードと異なる必要があります。";
    public static final String FAIL_UNEXPECTED_ERROR = "サーバーエラーによりパスワード変更に失敗しました。";
    public static final String FAIL_PASSWORD_USED_LAST_6_MONTHS = "過去6か月以内に使用したパスワードは再利用できません。";

}