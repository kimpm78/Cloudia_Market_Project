package com.cloudia.backend.common.model.pg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PG決済領収書（伝票）照会リクエストDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PGReceiptRequest {
    private String tid;         // 取引番号
    private String pgType;      // 決済システム種別
}
