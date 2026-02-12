package com.cloudia.backend.common.model.pg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PG ENC_DATA 復号リクエスト
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PGDecryptRequest {

    /** PG returnUrl から渡された encData */
    private String encData;

    /** PG種別（CookiePay / TOSS / INICIS などの区分用） */
    private String pgType;
}
