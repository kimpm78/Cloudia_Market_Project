package com.cloudia.backend.CM_01_1007.constants;

public final class CM011007MessageConstant {

    private CM011007MessageConstant() {
    }

    public static final String CONTROLLER_GET_PROFILE_START = "プロフィール情報取得リクエスト開始: loginId={}";
    public static final String CONTROLLER_GET_PROFILE_END = "プロフィール情報取得リクエスト完了: loginId={}";
    public static final String CONTROLLER_UPDATE_PROFILE_START = "プロフィール情報更新リクエスト開始: loginId={}";
    public static final String CONTROLLER_UPDATE_PROFILE_END = "プロフィール情報更新リクエスト完了: loginId={}";
    public static final String SERVICE_GET_PROFILE = "サービス: プロフィール情報取得, loginId={}";
    public static final String SERVICE_UPDATE_PROFILE = "サービス: プロフィール情報更新, loginId={}";
    public static final String SUCCESS_UPDATE_PROFILE = "プロフィールが正常に更新されました。";
    public static final String FAIL_USER_NOT_FOUND = "ユーザーが見つかりません。";
}