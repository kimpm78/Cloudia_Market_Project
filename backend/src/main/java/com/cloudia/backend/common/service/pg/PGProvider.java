package com.cloudia.backend.common.service.pg;

import com.cloudia.backend.common.model.pg.PGReadyRequest;
import com.cloudia.backend.common.model.pg.PGApproveRequest;
import com.cloudia.backend.common.model.pg.PGCancelRequest;
import com.cloudia.backend.common.model.pg.PGDecryptRequest;
import com.cloudia.backend.common.model.pg.PGPayCertRequest;
import com.cloudia.backend.common.model.pg.PGReceiptRequest;
import com.cloudia.backend.common.model.pg.PGResult;

/**
 * PGProviderインターフェース
 */
public interface PGProvider {

    /**
     * 決済準備（READY）
     */
    PGResult ready(PGReadyRequest request);

    /**
     * 決済承認（APPROVE）
     */
    PGResult approve(PGApproveRequest request);

    /**
     * 決済キャンセル（CANCEL）
     */
    PGResult cancel(PGCancelRequest request);

    /**
     * ENC_DATAの復号
     */
    PGResult decrypt(PGDecryptRequest request);

    /**
     * 金額検証（PayCert）
     */
    PGResult payCert(PGPayCertRequest request);

    /**
     * 伝票出力／照会
     */
    PGResult receipt(PGReceiptRequest request);

    /**
     * Providerタイプを返却（例: COOKIEPAY, TOSS）
     */
    String getProviderType();
}
