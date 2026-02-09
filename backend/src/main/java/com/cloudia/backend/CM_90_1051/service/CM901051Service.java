package com.cloudia.backend.CM_90_1051.service;

import java.util.List;

import com.cloudia.backend.CM_90_1051.model.AddressDto;
import com.cloudia.backend.CM_90_1051.model.OrderDetailDto;
import com.cloudia.backend.CM_90_1051.model.OrderDto;
import com.cloudia.backend.CM_90_1051.model.SearchRequestDto;

public interface CM901051Service {
    /**
     * 주문 전체 리스트 조회
     * 
     * @return 주문 전체 리스트
     */
    List<OrderDto> findByAllOrders();

    /**
     * 특정 주문 리스트 조회
     * 
     * @return 특정 주문 리스트
     */
    List<OrderDto> getFindOrders(SearchRequestDto searchRequest);

    /**
     * 특정 주문 상세 리스트 조회
     * 
     * @return 특정 주문 상세 리스트
     */
    List<OrderDetailDto> getFindOrderDetail(SearchRequestDto searchRequest);

    /**
     * 정산 상태 업데이트
     * 
     * @return 성공 여부
     */
    Integer uptStatus(SearchRequestDto searchRequest, String userId);

    /**
     * 배송지 정보
     * 
     * @return 배송지 정보
     */
    AddressDto getAddress(SearchRequestDto searchRequest);
}
