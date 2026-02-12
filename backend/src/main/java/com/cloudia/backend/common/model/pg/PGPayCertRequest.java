package com.cloudia.backend.common.model.pg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PG決済金額検証リクエスト（PayCert）
 * - 決済の改ざん防止のため、PGが提供する金額検証API用のDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PGPayCertRequest {

    /** PG取引番号（TID） */
    private String tid;

    /** 検証対象の決済金額 */
    private Integer amount;

    /** PG種別（CookiePay / TOSS / INICIS など） */
    private String pgType;
}
