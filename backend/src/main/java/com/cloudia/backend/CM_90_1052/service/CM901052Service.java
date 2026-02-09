package com.cloudia.backend.CM_90_1052.service;

import java.util.List;

import com.cloudia.backend.CM_90_1052.model.OrderDetailDto;
import com.cloudia.backend.CM_90_1052.model.RefundRequestDto;
import com.cloudia.backend.CM_90_1052.model.RefundSearchRequestDto;
import com.cloudia.backend.CM_90_1052.model.ReturnsDto;

public interface CM901052Service {
    /**
     * 환불/교환 리스트 조회
     * 
     * @return 환불/교환 리스트
     */
    List<ReturnsDto> getRefund();

    /**
     * 환불/교환 리스트 조회
     * 
     * @return 환불/교환 리스트
     */
    List<ReturnsDto> getPeriod(RefundSearchRequestDto searchDto);

    /**
     * 환불 상품 리스트
     * 
     * @param requestNo    요청 번호
     * @param refundNumber 사원 번호
     * @param orderNumber  주문 번호
     * @return 환불 상품 리스트
     */
    List<OrderDetailDto> getOrderDetail(String requestNo,
            String refundNumber,
            String orderNumber);

    /**
     * 환불 진행 처리
     * 
     * @param requestNo 환불 정보
     * @return 환불 진행 업데이트
     */
    Integer updateRefund(RefundRequestDto requestDto, String userId);
}
