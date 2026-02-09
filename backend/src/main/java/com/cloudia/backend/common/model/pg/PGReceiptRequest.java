package com.cloudia.backend.common.model.pg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PG 결제 영수증(전표) 조회 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PGReceiptRequest {

    /** PG 거래 번호 (TID) */
    private String tid;

    /** PG 종류 구분 (COOKIEPAY, TOSS, INICIS 등) */
    private String pgType;
}
