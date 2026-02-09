package com.cloudia.backend.CM_90_1052.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_90_1052.mapper.CM901052Mapper;
import com.cloudia.backend.CM_90_1052.model.OrderDetailDto;
import com.cloudia.backend.CM_90_1052.model.RefundRequestDto;
import com.cloudia.backend.CM_90_1052.model.RefundSearchRequestDto;
import com.cloudia.backend.CM_90_1052.model.ReturnDetailsDto;
import com.cloudia.backend.CM_90_1052.model.ReturnsDto;
import com.cloudia.backend.CM_90_1052.service.CM901052Service;
import com.cloudia.backend.common.exception.ErrorCode;
import com.cloudia.backend.common.exception.InvalidRequestException;
import com.cloudia.backend.common.log.LogHelper;
import com.cloudia.backend.common.log.LogMessage;
import com.cloudia.backend.common.util.DateCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM901052ServiceImpl implements CM901052Service {
    private final CM901052Mapper cm901052Mapper;
    private final DateCalculator dateCalculator;

    /**
     * 환불/교환 리스트 조회
     * 
     * @return 환불/교환 리스트
     */
    public List<ReturnsDto> getRefund() {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "교환/환불 조회" });
        List<ReturnsDto> result = cm901052Mapper.getRefunds();

        if (result == null) {
            result = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "교환/환불 조회", String.valueOf(result.size()) });

        return result;
    }

    /**
     * 환불/교환 리스트 조회
     * 
     * @return 환불/교환 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReturnsDto> getPeriod(RefundSearchRequestDto searchDto) {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "교환/환불 조회" });
        List<ReturnsDto> result = cm901052Mapper.getPeriod(searchDto);

        if (result == null) {
            result = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "교환/환불 조회", String.valueOf(result.size()) });

        return result;
    }

    /**
     * 환불/환불 상품 리스트
     * 
     * @param requestNo    요청 번호
     * @param refundNumber 사원 번호
     * @param orderNumber  주문 번호
     * @return 환불/환불 상품 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDetailDto> getOrderDetail(String requestNo,
            String refundNumber,
            String orderNumber) {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "교환/환불 조회" });
        int refundCount = cm901052Mapper.getRefundCount(requestNo, refundNumber);

        if (refundCount == 0) {
            LogHelper.log(LogMessage.COMMON_SELECT_EMPTY, new String[] { "교환/환불 조회" });
            throw new InvalidRequestException(ErrorCode.VALIDATION_SEARCH_TERM_EMPTY);
        }

        List<OrderDetailDto> result = cm901052Mapper.getOrderDetail(refundNumber, orderNumber);

        if (result == null) {
            result = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "교환/환불 조회", String.valueOf(result.size()) });

        return result;
    }

    /**
     * 환불 진행 처리
     * 
     * @param requestNo 환불 정보
     * @param userId    업데이트자
     * @return 환불 진행 업데이트
     */
    @Override
    @Transactional
    public Integer updateRefund(RefundRequestDto requestDto, String userId) {
        int result = refund(requestDto, userId);
        result += refundDetail(requestDto, userId);
        return result;
    }

    /**
     * 환불 업데이트
     * 
     * @param requestNo 환불 정보
     * @param userId    업데이트자
     * @return 환불 진행 업데이트
     */
    private Integer refund(RefundRequestDto requestDto, String userId) {
        ReturnsDto returnsDto = new ReturnsDto();
        returnsDto.setCustomerId(requestDto.getRefundNumber());
        returnsDto.setOrderNo(requestDto.getRequestNo());
        returnsDto.setReason(requestDto.getMemo());
        returnsDto.setTotalAmount(requestDto.getTotalAmount());
        returnsDto.setRefundAmount(requestDto.getProductTotalAmount());
        if (requestDto.getRefundType() == "0") {
            returnsDto.setReturnStatusValue(2);
        } else {
            returnsDto.setReturnStatusValue(4);
        }
        if (requestDto.getShippingFee() == "0") {
            returnsDto.setShippingFeeSellerAmount(requestDto.getShippingAmount());
        } else {
            returnsDto.setShippingFeeCustomerAmount(requestDto.getShippingAmount());
        }
        returnsDto.setUpdatedBy(userId);
        returnsDto.setUpdatedAt(dateCalculator.tokyoTime());

        return cm901052Mapper.updateRefund(returnsDto);
    }

    /**
     * 환불 상세 생성
     * 
     * @param requestNo 환불 정보
     * @param userId    업데이트자
     * @return 환불 진행 생성
     */
    private Integer refundDetail(RefundRequestDto requestDto, String userId) {
        int result = 0;
        ReturnsDto refundInfo = cm901052Mapper.getRefund(requestDto.getRequestNo(), requestDto.getRefundNumber());

        for (RefundRequestDto.RefundProductDto item : requestDto.getProducts()) {
            ReturnDetailsDto detailsDto = new ReturnDetailsDto();
            detailsDto.setReturnId(refundInfo.getReturnId());
            detailsDto.setUnitPrice(item.getUnitPrice());
            detailsDto.setQuantity(item.getQuantity());
            detailsDto.setProductCode(item.getProductName());
            detailsDto.setCreatedBy(userId);
            detailsDto.setCreatedAt(dateCalculator.tokyoTime());
            detailsDto.setUpdatedBy(userId);
            detailsDto.setUpdatedAt(dateCalculator.tokyoTime());

            result += cm901052Mapper.updateRefundDetail(detailsDto);
        }

        return result;
    }
}
