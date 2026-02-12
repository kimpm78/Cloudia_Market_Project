package com.cloudia.backend.common.model.pg;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * PG決済承認リクエスト共通モデル
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PGApproveRequest {

    private Long orderId;        // 注文ID（内部PK、任意）
    private String orderNumber;  // 注文番号（加盟店注文番号）
    private String pgType;       // PG種別: "COOKIEPAY", "TOSS", "INICIS" など
    private String tid;          // PG取引番号（TID、payments.transaction_id に保存）
    private Integer amount;      // 承認金額（検証用）
}
