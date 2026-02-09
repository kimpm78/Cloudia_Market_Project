package com.cloudia.backend.CM_01_1008.constants;

public final class CM011008MessageConstant {

    private CM011008MessageConstant() {
    }

    public static final String GET_ADDRESSES_START = "住所録取得リクエスト開始: ユーザー={}";
    public static final String GET_ADDRESSES_END = "住所録取得リクエスト完了: ユーザー={}";

    public static final String ADD_ADDRESS_START = "住所追加リクエスト開始: ユーザー={}";
    public static final String ADD_ADDRESS_END = "住所追加リクエスト完了: ユーザー={}";

    public static final String UPDATE_ADDRESS_START = "住所更新リクエスト開始: addressId={}";
    public static final String UPDATE_ADDRESS_END = "住所更新リクエスト完了: addressId={}";

    public static final String DELETE_ADDRESS_START = "住所削除リクエスト開始: addressId={}";
    public static final String DELETE_ADDRESS_END = "住所削除リクエスト完了: addressId={}";

    public static final String SERVICE_GET_ADDRESSES = "サービス: ユーザー住所録取得, ユーザー={}";
    public static final String SERVICE_ADD_ADDRESS = "サービス: 住所追加, ユーザー={}";
    public static final String SERVICE_UPDATE_ADDRESS = "サービス: 住所更新, addressId={}";
    public static final String SERVICE_DELETE_ADDRESS = "サービス: 住所削除, addressId={}";

    public static final String SUCCESS_ADD_ADDRESS = "住所が正常に登録されました。";
    public static final String SUCCESS_UPDATE_ADDRESS = "住所が正常に更新されました。";

    public static final String FAIL_USER_NOT_FOUND = "ユーザーが見つかりません。";
    public static final String FAIL_ADDRESS_LIMIT_EXCEEDED = "住所は最大3件まで登録できます。";
    public static final String FAIL_ADDRESS_NOT_FOUND = "住所が見つかりません。";
    public static final String FAIL_ADDRESS_FORBIDDEN = "この住所にアクセスできません。";

}
