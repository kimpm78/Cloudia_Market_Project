package com.cloudia.backend.common.service.pg.impl;

import com.cloudia.backend.common.model.pg.*;
import com.cloudia.backend.common.service.pg.PGProvider;
import com.cloudia.backend.common.service.pg.client.CookiepayClientImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookiePayPGProvider implements PGProvider {

    private final CookiepayClientImpl client;

    @Override
    public String getProviderType() {
        return "COOKIEPAY";
    }

    /**
     * READY
     * @param req
     * @return
     */
    @Override
    public PGResult ready(PGReadyRequest req) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("ORDERNO", req.getOrderNo());
        payload.put("PRODUCTNAME", req.getProductName());
        payload.put("AMOUNT", req.getAmount());
        payload.put("BUYERNAME", req.getBuyerName());
        payload.put("BUYEREMAIL", req.getBuyerEmail());
        payload.put("RETURNURL", req.getReturnUrl());
        payload.put("HOMEURL", req.getHomeUrl());
        payload.put("PAYMETHOD", req.getPayMethod());
        payload.put("PAYTYPE", req.getPayType());
        payload.put("TAXYN", req.getTaxYn() != null ? req.getTaxYn() : "Y");
        payload.put("CANCELURL", req.getCancelUrl());
        payload.put("FAILURL", req.getFailUrl());
        payload.put("TAXFREEAMOUNT", req.getTaxFreeAmount());
        payload.put("QUOTA", req.getQuota());
        payload.put("CARDLIST", req.getCardList());
        payload.put("ENC_YN", req.getEncYn());

        return client.ready(req, payload);
    }

    /**
     * 승인
     * @param req
     * @return
     */
    @Override
    public PGResult approve(PGApproveRequest req) {

        Map<String, Object> payload = new HashMap<>();

        if (req.getTid() != null) {
            payload.put("tid", req.getTid());
        }

        return client.approve(payload);
    }

    /**
     * 취소
     * @param req
     * @return
     */
    @Override
    public PGResult cancel(PGCancelRequest req) {

        Map<String, Object> payload = new HashMap<>();

        // 필수값
        if (req.getTid() != null && !req.getTid().isBlank()) {
            payload.put("tid", req.getTid());
        }
        if (req.getAmount() != null) {
            payload.put("amount", req.getAmount());
        }
        if (req.getReason() != null && !req.getReason().isBlank()) {
            payload.put("reason", req.getReason());
        }

        return client.cancel(payload);
    }

    /**
     * 암호화 데이터 복호화
     * @param req
     * @return
     */
    @Override
    public PGResult decrypt(PGDecryptRequest req) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("ENC_DATA", req.getEncData());

        return client.decrypt(payload);
    }

    /**
    * 금액 검증
    * @param req
    * @return
    */
    @Override
    public PGResult payCert(PGPayCertRequest req) {

        Map<String, Object> payload = new HashMap<>();

        payload.put("TID", req.getTid());
        payload.put("AMOUNT", req.getAmount());

        return client.payCert(payload);
    }

    /**
     * 전표 조회
     * @param req
     * @return
     */
    @Override
    public PGResult receipt(PGReceiptRequest req) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("TID", req.getTid());

        return client.receipt(payload);
    }
}
