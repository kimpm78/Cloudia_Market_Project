package com.cloudia.backend.CM_06_1000.constants;

public class CM061000MessageConstant {
    private CM061000MessageConstant() {}
    // 取得
    public static final String CART_FETCH_SUCCESS = "カートの取得に成功しました。";
    public static final String CART_FETCH_EMPTY   = "カートに商品が入っていません。";

    // 追加
    public static final String CART_ADD_SUCCESS = "商品をカートに追加しました。";
    public static final String CART_ADD_FAIL    = "カートへの追加中にエラーが発生しました。";

    // 数量変更
    public static final String CART_UPDATE_QTY_SUCCESS = "カートの数量を変更しました。";
    public static final String CART_UPDATE_QTY_FAIL    = "カート数量の変更中にエラーが発生しました。";

    // 削除
    public static final String CART_DELETE_SUCCESS = "カートの商品を削除しました。";
    public static final String CART_DELETE_FAIL    = "カート商品の削除中にエラーが発生しました。";

    // 在庫・バリデーション
    public static final String CART_OUT_OF_STOCK      = "在庫がありません。";
    public static final String CART_ITEM_NOT_FOUND    = "カート情報が見つかりません。";
    public static final String CART_INVALID_QUANTITY  = "数量は1個以上である必要があります。";
    public static final String CART_LIMIT_EXCEEDED    = "カートには最大10個まで追加できます。";
    public static final String CART_PRODUCT_LIMIT_EXCEEDED = "同一商品は最大 %d 個まで追加できます。";
    public static final String CART_RESERVATION_MONTH_MISMATCH_ON_ADD =
            "発売月が異なる予約商品と通常商品は一緒に追加できません。";
    public static final String CART_RESERVATION_MONTH_UNKNOWN_ON_ADD =
            "発売月情報を確認できないため、予約商品と通常商品を一緒に追加できません。";
    public static final String CART_RESERVATION_ONLY_MONTH_MISMATCH_ON_ADD =
            "発売月が異なる予約商品同士は一緒に追加できません。";

    // 注文準備（選択項目）
    public static final String CART_PREPARE_ORDER_SUCCESS = "選択した商品で注文準備を行いました。";
    public static final String CART_PREPARE_ORDER_EMPTY   = "選択されたカート商品がありません。";
    public static final String CART_PREPARE_ORDER_FAIL    = "注文準備中にエラーが発生しました。";
    public static final String CART_MIXED_ORDER_NOT_ALLOWED = "通常商品と予約商品は一緒に決済できません。";
    public static final String CART_RESERVATION_MONTH_MISMATCH =
            "予約商品は同一の発売月の商品同士のみ一緒に決済できます。";
    public static final String CART_RESERVATION_MONTH_UNKNOWN =
            "予約商品の発売月情報を確認できません。";

    // バリデーション＋共通エラーログ
    public static final String CART_VALIDATION_FAIL   = "カートのバリデーションに失敗しました: {}";
    public static final String CART_DB_ERROR          = "カート処理中にDBエラーが発生しました: {}";
}
