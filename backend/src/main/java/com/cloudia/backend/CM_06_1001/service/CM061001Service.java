package com.cloudia.backend.CM_06_1001.service;

import com.cloudia.backend.CM_06_1001.model.OrderCreate;
import com.cloudia.backend.CM_06_1001.model.OrderSummary;
import com.cloudia.backend.CM_06_1001.model.PaymentInfo;

public interface CM061001Service {

    /**
     * 주문 생성
     * @param request 주문 생성 요청 정보
     * @return 생성된 주문 요약 정보
     */
    OrderSummary createOrder(OrderCreate request);

    /**
     * 주문 완료 처리
     *
     * @param orderId 주문 ID
     */
    void completeOrder(Long orderId, String memberNumber);

    /**
     * 주문 요약 정보 조회
     *
     * @param orderId      주문 ID
     * @param memberNumber 회원 번호 (권한 검증용)
     * @return 주문 요약 정보
     */
    OrderSummary getOrderSummary(Long orderId, String memberNumber);

    /**
     * 최신 결제 상태 조회 (orderNumber 기준)
     *
     * @param orderNumber 주문번호
     * @return 최신 결제 정보
     */
    PaymentInfo findLatestPayment(String orderNumber);
}
