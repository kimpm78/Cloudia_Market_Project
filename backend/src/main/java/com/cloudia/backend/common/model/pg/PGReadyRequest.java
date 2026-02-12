package com.cloudia.backend.common.model.pg;

import lombok.Data;

/**
 * PG決済リクエスト（Ready）DTO
 */
@Data
public class PGReadyRequest {

    /**
     * 内部システム（加盟店）の注文情報
     */
    private Long orderId;             // 内部注文PK
    private String orderNumber;       // 内部注文番号（加盟店注文番号）
    private String orderNo;           // PGへ渡される注文番号（通常は orderNumber と同一）

    /**
     * PG必須リクエスト情報
     */
    private String productName;       // 商品名（CookiePay：一部特殊文字に制限あり）
    private Integer amount;           // 決済金額
    private String buyerName;         // 決済者名
    private String buyerEmail;        // 決済者メール（任意だが通常使用）
    private String returnUrl;         // PG決済結果のcallback URL
    private String homeUrl;           // 決済完了後の遷移先URL
    private String cancelUrl;         // 決済途中キャンセル時の遷移先URL
    private String failUrl;           // PG決済失敗時の遷移先URL
    private String pgType;            // PG種別（"COOKIEPAY", "TOSS", "INICIS" など）

    /**
     * PG任意リクエスト情報（PGポリシーにより必要時に使用）
     */
    private Integer taxFreeAmount;    // 非課税金額（複合課税専用）
    private String directResultFlag;  // Firefox のクロスドメイン問題対応（KiwoomPay）
    private String mtype;             // WebView決済区分（KiwoomPay／OneGlobalPay）
    private String payMethod;         // 決済手段（CARD/BANK）
    private String quota;             // カード分割期間（00＝一括払い）
    private String buyerId;           // 内部顧客ID
    private String taxYn;             // 課税／非課税区分（Y/N/M）
    private String closeUrl;          // キャンセル後の遷移URL（KakaoPay）
    private String escrow;            // エスクロー決済可否
    private String engFlag;           // 海外英語決済画面表示可否（KiwoomPay）
    private String payType;           // 海外決済／特殊カードタイプ（CookiePay PAY_TYPE）
    private String cardList;          // 海外カード種別選択
    private String encYn;             // 暗号化リターン可否（Y＝暗号化データ使用）
}