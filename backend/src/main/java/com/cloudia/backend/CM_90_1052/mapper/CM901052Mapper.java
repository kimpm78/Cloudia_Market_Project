package com.cloudia.backend.CM_90_1052.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1052.model.OrderDetailDto;
import com.cloudia.backend.CM_90_1052.model.RefundSearchRequestDto;
import com.cloudia.backend.CM_90_1052.model.ReturnDetailsDto;
import com.cloudia.backend.CM_90_1052.model.ReturnsDto;

@Mapper
public interface CM901052Mapper {
    /**
     * 환불/교환 리스트 조회
     * 
     * @return 환불/교환 리스트
     */
    List<ReturnsDto> getRefunds();

    /**
     * 환불/교환 리스트 조회
     * 
     * @return 환불/교환 리스트
     */
    List<ReturnsDto> getPeriod(RefundSearchRequestDto searchDto);

    /**
     * 요청 번호 조회
     * 
     * @param requestNo    요청 번호
     * @param refundNumber 사원 번호
     * @return 요청번호 여부
     */
    int getRefundCount(@Param("requestNo") String requestNo,
            @Param("refundNumber") String refundNumber);

    /**
     * 요청 번호 조회
     * 
     * @param requestNo    요청 번호
     * @param refundNumber 사원 번호
     * @return 요청번호 여부
     */
    ReturnsDto getRefund(@Param("requestNo") String requestNo,
            @Param("refundNumber") String refundNumber);

    /**
     * 환불/환불 상품 리스트
     * 
     * @param refundNumber 사원 번호
     * @param orderNumber  주문 번호
     * @return 환불/환불 상품 리스트
     */
    List<OrderDetailDto> getOrderDetail(@Param("refundNumber") String refundNumber,
            @Param("orderNumber") String orderNumber);

    /**
     * 환불 업데이트
     * 
     * @param requestDto 환불 정보
     * @return 환불 진행 업데이트
     */
    int updateRefund(ReturnsDto requestDto);

    /**
     * 환불 상세 생성
     * 
     * @param requestDto 환불 정보
     * @return 환불 진행 생성
     */
    int updateRefundDetail(ReturnDetailsDto requestDto);
}
