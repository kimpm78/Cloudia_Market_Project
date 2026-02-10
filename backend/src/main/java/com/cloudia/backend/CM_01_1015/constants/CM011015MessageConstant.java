package com.cloudia.backend.CM_01_1015.constants;

public final class CM011015MessageConstant {
    private CM011015MessageConstant() {
    }
    public static final String LOG_RETURN_HISTORY_REQUEST =
        "交換・返品履歴一覧の照会リクエスト - ユーザー: {}";

    public static final String LOG_RETURN_DETAIL_REQUEST =
        "交換・返品詳細の照会リクエスト - 返品ID: {}, ユーザー: {}";

    public static final String LOG_RETURN_CREATE_REQUEST =
        "新規交換・返品申請の受付 - ユーザー: {}, タイトル: {}";

    public static final String LOG_ORDER_PRODUCTS_REQUEST =
        "申請用注文商品の照会リクエスト - 注文番号: {}, ユーザー: {}";

    public static final String LOG_RETURNABLE_ORDERS_REQUEST =
        "申請可能な注文一覧の照会リクエスト - ユーザー: {}";
}
