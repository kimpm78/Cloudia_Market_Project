package com.cloudia.backend.common.model.pg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PG 결제 금액 검증 요청(PayCert)
 * - 결제 위/변조 방지를 위해 PG에서 제공하는 금액 검증 API용 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PGPayCertRequest {

    /** PG 거래 번호 (TID) */
    private String tid;

    /** 검증할 결제 금액 */
    private Integer amount;

    /** PG 종류 (쿠키페이 / 토스 / 이니시스 등) */
    private String pgType;
}
