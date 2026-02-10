package com.cloudia.backend.CM_90_1051.constants;

public class CM901051Constant {
    public static final int ORDER_STATUS_ALL = 0; // 全体
    public static final int ORDER_STATUS_REMITTANCE_PENDING = 1; // 入金確認中
    public static final int ORDER_STATUS_CONFIRMED = 2; // 購入確定
    public static final int ORDER_STATUS_PREPARING_SHIPMENT = 3; // 発送準備中
    public static final int ORDER_STATUS_SHIPPING = 4; // 配送中
    public static final int ORDER_STATUS_DELIVERED = 5; // 配送完了
    public static final int ORDER_STATUS_CANCELED = 6; // 購入キャンセル

    public static final int PAYMENT_METHOD_BANK_TRANSFER = 1; // 銀行振込
    public static final int PAYMENT_METHOD_CREDIT_CARD = 2; // クレジットカード
    public static final String PAYMENT_METHOD_BANK_TRANSFER_STRING = "銀行振込"; // 銀行振込
    public static final String PAYMENT_METHOD_CREDIT_CARD_STRING = "クレジットカード"; // クレジットカード
}
