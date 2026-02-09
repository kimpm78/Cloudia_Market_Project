package com.cloudia.backend.CM_01_1005.service;

import com.cloudia.backend.CM_01_1005.model.OrderDetailResponse;
import com.cloudia.backend.CM_01_1005.model.OrderListResponse;
import com.cloudia.backend.CM_01_1005.model.OrderCancelRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface CM011005Service {

    /**
     * 로그인 ID와 검색 조건에 맞는 주문 이력을 조회
     */
    ResponseEntity<List<OrderListResponse>> searchOrderHistory(String loginId, Map<String, Object> filters);

    /**
     * 특정 주문의 상세 내역을 조회
     */
    ResponseEntity<OrderDetailResponse> getOrderDetail(String loginId, String orderNo);

    /**
     * 주문 취소 요청 (신용카드 취소 또는 계좌 환불 요청)
     */
    ResponseEntity<String> cancelOrder(String loginId, OrderCancelRequest request);

    /**
     * 사용자의 배송지 목록 조회 (배송지 변경 모달용)
     */
    ResponseEntity<List<Map<String, Object>>> getDeliveryAddresses(String loginId);

    /**
     * 주문 배송지 정보 수정 (구매 확정 전까지만 가능)
     */
    ResponseEntity<String> updateShippingInfo(String loginId, Map<String, Object> params);
}