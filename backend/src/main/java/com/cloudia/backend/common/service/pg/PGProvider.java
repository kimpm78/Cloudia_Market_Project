package com.cloudia.backend.common.service.pg;

import com.cloudia.backend.common.model.pg.PGReadyRequest;
import com.cloudia.backend.common.model.pg.PGApproveRequest;
import com.cloudia.backend.common.model.pg.PGCancelRequest;
import com.cloudia.backend.common.model.pg.PGDecryptRequest;
import com.cloudia.backend.common.model.pg.PGPayCertRequest;
import com.cloudia.backend.common.model.pg.PGReceiptRequest;
import com.cloudia.backend.common.model.pg.PGResult;

/**
 * PGProvider 인터페이스
 */
public interface PGProvider {

    /**
     * 결제 준비(READY)
     */
    PGResult ready(PGReadyRequest request);

    /**
     * 결제 승인(APPROVE)
     */
    PGResult approve(PGApproveRequest request);

    /**
     * 결제 취소(CANCEL)
     */
    PGResult cancel(PGCancelRequest request);

    /**
     * ENC_DATA 복호화
     */
    PGResult decrypt(PGDecryptRequest request);

    /**
     * 금액 검증 (PayCert)
     */
    PGResult payCert(PGPayCertRequest request);

    /**
     * 전표 출력/조회
     */
    PGResult receipt(PGReceiptRequest request);

    /**
     * Provider 타입 반환 (예: COOKIEPAY, TOSS)
     */
    String getProviderType();
}
