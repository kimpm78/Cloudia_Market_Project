package com.cloudia.backend.common.service;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.model.pg.PGApproveRequest;
import com.cloudia.backend.common.model.pg.PGCancelRequest;
import com.cloudia.backend.common.model.pg.PGDecryptRequest;
import com.cloudia.backend.common.model.pg.PGFailRequest;
import com.cloudia.backend.common.model.pg.PGReadyRequest;

import java.util.Map;

/**
 * 결제 서비스 (PG 연동 공통 처리)
 * 각 PGProvider(쿠키페이 등)의 구현체는 PGProviderRegistry를 통해 선택됨
 */
public interface PaymentService {

    /**
     * PG 결제요청 (결제창 진입)
     *
     * @param request PG 결제 준비 요청정보
     * @return ResponseModel (PG ready 결과 + redirect/form 구성 데이터)
     */
    ResponseModel<Map<String, Object>> ready(PGReadyRequest request);


    /**
     * 결제 승인/검증 단계
     *
     * @param request PG 승인 요청 정보
     * @return ResponseModel (결제 승인 결과)
     */
    ResponseModel<Map<String, Object>> approve(PGApproveRequest request);


    /**
     * 결제 취소 처리
     *
     * @param request 취소 요청 정보
     * @return ResponseModel (취소 결과)
     */
    ResponseModel<Map<String, Object>> cancel(PGCancelRequest request);

    /**
     * 결제 실패(또는 사용자 닫힘) 처리
     *
     * @param request 실패 처리 요청 정보
     * @return ResponseModel (실패 처리 결과)
     */
    ResponseModel<Map<String, Object>> fail(PGFailRequest request);


    /**
     * PG CALLBACK(RETURN URL) 처리
     *
     * @param queryParams PG에서 전달되는 파라미터 (encData, orderNo 등)
     * @return ResponseModel (callback 처리 결과 반환)
     */
    ResponseModel<Map<String, Object>> callback(Map<String, String> queryParams);

    /**
     * PG ENC_DATA 복호화
     *
     * @param request 복호화 요청 정보
     * @return ResponseModel (복호화 결과)
     */
    ResponseModel<Map<String, Object>> decrypt(PGDecryptRequest request);
}
