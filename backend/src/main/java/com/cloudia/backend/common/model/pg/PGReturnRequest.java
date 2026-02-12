package com.cloudia.backend.common.model.pg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PG ReturnUrl リクエストDTO
 * PG決済完了後に our-server へ渡されるデータを保持するモデル
 * encData を基点に decrypt → approve へと続く中核構造
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PGReturnRequest {

    /** PG returnUrl から渡される暗号化データ */
    private String encData;

    /** PG returnUrl から直接渡される注文番号 */
    private String orderNo;

    /** PG種別（COOKIEPAY, TOSS, INICIS など） */
    private String pgType;

    /** PGがreturn時に併せて渡す可能性のあるコード／メッセージ（任意） */
    private String resultCode;     // 直接渡される場合あり
    private String resultMessage;  // 直接渡される場合あり
}
