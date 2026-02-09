package com.cloudia.backend.CM_90_1051.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.cloudia.backend.CM_90_1051.model.AddressDto;
import com.cloudia.backend.CM_90_1051.model.OrderDetailDto;
import com.cloudia.backend.CM_90_1051.model.OrderDto;
import com.cloudia.backend.CM_90_1051.model.SearchRequestDto;

@Mapper
public interface CM901051Mapper {
    /**
     * 주문 전체 리스트 조회
     * 
     * @return 주문 전체 리스트
     */
    List<OrderDto> findByAllOrders();

    /**
     * 특정 주문 전체 리스트 조회
     * 
     * @param 검색 데이터
     * @return 특정 주문 리스트
     */
    List<OrderDto> getFindOrders(SearchRequestDto searchRequest);

    /**
     * 특정 주문 전체 리스트 조회
     * 
     * @param 검색 데이터
     * @return 특정 주문 리스트
     */
    List<OrderDto> getFindOrder(SearchRequestDto searchRequest);

    /**
     * 특정 주문 상세 리스트 조회
     * 
     * @param 검색 데이터
     * @return 특정 주문 상세 리스트
     */
    List<OrderDetailDto> getFindOrderDetail(SearchRequestDto searchRequest);

    /**
     * 정산 상태 업데이트
     * 
     * @param 검색 데이터
     * @return 성공 여부
     */
    int uptStatus(OrderDto entity);

    /**
     * 배송지 정보
     * 
     * @return 배송지 정보
     */
    AddressDto getAddress(SearchRequestDto searchRequest);
}
