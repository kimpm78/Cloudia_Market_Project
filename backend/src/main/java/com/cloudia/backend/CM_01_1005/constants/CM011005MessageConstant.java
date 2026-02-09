package com.cloudia.backend.CM_01_1005.constants;

public final class CM011005MessageConstant {

    private CM011005MessageConstant() {
    }
    public static final String CONTROLLER_GET_HISTORY_START = "購入履歴の取得リクエスト開始: loginId={}, Filters={}";
    public static final String CONTROLLER_GET_HISTORY_END = "購入履歴の取得リクエスト完了（{}件）: loginId={}";
    public static final String SERVICE_SEARCH_HISTORY = "サービス: 注文履歴検索, loginId={}";
    public static final String FAIL_ORDER_NOT_FOUND = "注文履歴が見つかりません。";
    public static final String SUCCESS_ORDER_LOADED = "注文履歴の取得に成功しました。";
}