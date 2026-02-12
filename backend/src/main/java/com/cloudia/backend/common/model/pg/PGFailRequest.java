package com.cloudia.backend.common.model.pg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 決済失敗（またはユーザーによる画面クローズ／キャンセル）処理リクエストモデル
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PGFailRequest {
    /** 注文情報 */
    private Long orderId;
    private String orderNumber;

    /** PG取引情報（任意） */
    private String tid;

    /** PG種別 */
    private String pgType;

    /** 失敗理由（ログ／DB保存用） */
    private String reason;
}
