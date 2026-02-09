package com.cloudia.backend.common.model.pg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PG ENC_DATA 복호화 요청
 * - PG 결제 승인(return URL 등)에서 전달되는 암호화 데이터 복호화용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PGDecryptRequest {

    /** PG returnUrl 에서 넘어온 encData */
    private String encData;

    /** PG 종류 (쿠키페이 / 토스 / 이니시스 등 구분용) */
    private String pgType;
}
