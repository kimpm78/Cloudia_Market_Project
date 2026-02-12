package com.cloudia.backend.common.model.pg;

import lombok.Data;

/**
 * PG決済キャンセルリクエストDTO
 */
@Data
public class PGCancelRequest {

    /**
     * PG必須項目（CookiePay基準）
     */
    private String tid;                 // PG取引番号（TID）
    private Integer amount;             // キャンセル金額（部分／全額）
    private String reason;              // キャンセル理由
    private String pgType;              // PG種別（CookiePay）

    /**
     * 内部システムでDB更新のために必要な情報
     */
    private String paymentId;           // 内部 payments のPK
    private Long orderId;               // 内部 orders のPK
    private String orderNumber;         // 内部注文番号

    /**
     * 任意項目（一部PGのみ使用）
     */
    private Integer taxfreeAmt;         // 非課税キャンセル金額（複合課税時）
    private Integer cancelRemainAmount; // 残りキャンセル可能金額（WelcomePay専用）
    private String bank;                // 返金銀行コード（仮想口座の場合）
    private String accountNo;           // 返金口座番号（仮想口座の場合）
    private String accountName;         // 返金口座名義（仮想口座の場合）
}
