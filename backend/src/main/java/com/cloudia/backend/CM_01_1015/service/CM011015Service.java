package com.cloudia.backend.CM_01_1015.service;

import com.cloudia.backend.CM_01_1015.model.ReturnResponse;
import com.cloudia.backend.CM_01_1015.model.ReturnRequest;
import com.cloudia.backend.common.model.ResponseModel;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

public interface CM011015Service {

    /**
     * 교환/반품 내역 조회
     */
    ResponseModel<List<ReturnResponse>> getReturnHistory(String loginId);

    /**
     * 교환/반품 신청
     */
    ResponseEntity<ResponseModel<Object>> createReturnRequest(String loginId, ReturnRequest request);

    /**
     * 상세 조회
     */
    ResponseModel<ReturnResponse> getReturnDetail(String loginId, int returnId);

    /**
     * 주문별 상품 목록 조회
     */
    ResponseEntity<List<ReturnResponse.ProductInfo>> getOrderProducts(String loginId, String orderNo);

    /**
     * 신청 가능한 구매 확정 주문 목록 조회
     */
    ResponseModel<List<Map<String, Object>>> getReturnableOrderList(String loginId);
}