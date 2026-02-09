package com.cloudia.backend.CM_06_1001.constants;

public class CM061001MessageConstant {
    private CM061001MessageConstant() {}
    /**
     * 注文（Order）
     */
    public static final String ORDER_CREATE_SUCCESS = "注文が作成されました。";
    public static final String ORDER_CREATE_FAIL = "注文作成中にエラーが発生しました。";
    public static final String ORDER_PREPARE_SUCCESS = "注文の準備が完了しました。";
    public static final String ORDER_PREPARE_FAIL = "注文準備中にエラーが発生しました。";
    public static final String ORDER_COMPLETE_SUCCESS = "注文が完了しました。";
    public static final String ORDER_COMPLETE_FAIL = "注文完了処理中にエラーが発生しました。";
    public static final String ORDER_FETCH_SUCCESS = "注文情報を取得しました。";
    public static final String ORDER_FETCH_FAIL = "注文情報を取得できません。";
    public static final String ORDER_FORBIDDEN = "注文にアクセスする権限がありません。";

    /**
     * 決済（Payment）
     */
    public static final String PAYMENT_READY_SUCCESS = "決済の準備が完了しました。";
    public static final String PAYMENT_READY_FAIL = "決済準備中にエラーが発生しました。";
    public static final String PAYMENT_APPROVE_SUCCESS = "決済が承認されました。";
    public static final String PAYMENT_APPROVE_FAIL = "決済承認中にエラーが発生しました。";
    public static final String PAYMENT_CANCEL_SUCCESS = "決済がキャンセルされました。";
    public static final String PAYMENT_CANCEL_FAIL = "決済キャンセル中にエラーが発生しました。";

    /**
     * メール（Email）
     */
    public static final String EMAIL_START = "メール送信を開始します。";
    public static final String EMAIL_CUSTOMER_INFO_NOT_FOUND = "顧客のメール情報がないため、メールを送信できません。";
    public static final String EMAIL_SEND_SUCCESS = "メール送信成功 - 注文番号: {}";
    public static final String EMAIL_SEND_INPUT_ERROR = "メール送信の入力値エラー - 注文番号: {}, エラー: {}";
    public static final String EMAIL_SEND_SYSTEM_ERROR = "メール送信のシステムエラー - 注文番号: {}, エラー: {}";
    public static final String EMAIL_SEND_GENERAL_ERROR = "メール送信失敗 - 注文番号: {}, エラー: {}";
    public static final String EMAIL_SEND_FAILED_INPUT = "メール送信失敗: {}";
    public static final String EMAIL_SEND_FAILED_SYSTEM = "メール送信失敗: システムエラーが発生しました。";
    public static final String EMAIL_SEND_FAILED_GENERAL = "メール送信に失敗しました。";

    /**
     * 共通 / バリデーション / 例外
     */
    public static final String VALIDATION_FAIL = "リクエストのバリデーション失敗: {}";
    public static final String DB_ERROR = "注文／決済処理中にDBエラーが発生: {}";
    public static final String UNEXPECTED_ERROR = "注文／決済処理中に予期しないエラーが発生: {}";

    /**
     * 注文／決済のバリデーションおよび処理エラー
     */
    public static final String ORDER_REQUEST_REQUIRED = "注文情報が必要です。";
    public static final String USER_INFO_REQUIRED = "ユーザー情報が必要です。";
    public static final String MEMBER_NUMBER_REQUIRED = "会員番号が必要です。";
    public static final String CART_ITEMS_REQUIRED = "注文するカート項目が必要です。";
    public static final String CART_ITEMS_NOT_FOUND = "カート商品が見つかりません。";
    public static final String TOTAL_AMOUNT_INVALID = "合計金額が正しくありません。";
    public static final String SHIPPING_FEE_INVALID = "送料が正しくありません。";
    public static final String PAYMENT_METHOD_REQUIRED = "決済方法が必要です。";
    public static final String PAYMENT_METHOD_UNSUPPORTED = "対応していない決済方法です: {}";
    public static final String SHIPPING_ADDRESS_INVALID = "配送先情報が正しくありません。";
    public static final String ORDER_QUANTITY_INVALID = "注文数量が正しくありません。";
    public static final String PRODUCT_PRICE_INVALID = "商品価格情報が正しくありません。";
    public static final String STOCK_NOT_ENOUGH = "在庫不足のため注文を完了できません。";
    public static final String STOCK_NOT_FOUND = "在庫情報が見つかりません。";
    public static final String ORDER_NOT_FOUND = "注文情報が見つかりません。";
    public static final String ORDER_ID_REQUIRED = "注文IDは必須です。";
    public static final String ORDER_NUMBER_REQUIRED = "注文番号が必要です。";

    /**
     * ログメッセージ
     */
    public static final String LOG_ORDER_CREATED = "[注文作成] orderId={}, orderNumber={}";
    public static final String LOG_ORDER_SUMMARY_ACCESS_DENIED = "[ORDER SUMMARY] access denied. orderId={}, member={}";
    public static final String LOG_LATEST_PAYMENT = "[最新決済] orderNumber={}";
}
