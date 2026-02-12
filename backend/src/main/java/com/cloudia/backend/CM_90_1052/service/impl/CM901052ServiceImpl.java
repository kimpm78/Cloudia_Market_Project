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
     * 返金/交換リスト取得
     * 
     * @return 返金/交換リスト取得
     */
    public List<ReturnsDto> getRefund() {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "交換/返金 照会" });
        List<ReturnsDto> result = cm901052Mapper.getRefunds();

        if (result == null) {
            result = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "交換/返金 照会", String.valueOf(result.size()) });

        return result;
    }

    /**
     * 返金/交換リスト取得
     * 
     * @return 返金/交換リスト取得
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReturnsDto> getPeriod(RefundSearchRequestDto searchDto) {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "交換/返金 照会" });
        List<ReturnsDto> result = cm901052Mapper.getPeriod(searchDto);

        if (result == null) {
            result = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "交換/返金 照会", String.valueOf(result.size()) });

        return result;
    }

    /**
     * 返金/交換 商品リスト
     * 
     * @param requestNo    リクエスト番号
     * @param refundNumber 社員番号
     * @param orderNumber  注文番号
     * @return 返金/交換 商品リスト
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDetailDto> getOrderDetail(String requestNo,
            String refundNumber,
            String orderNumber) {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "交換/返金 照会" });
        String lookupRequestNo = (requestNo == null || requestNo.isBlank()) ? orderNumber : requestNo;
        int refundCount = cm901052Mapper.getRefundCount(lookupRequestNo, refundNumber);

        if (refundCount == 0) {
            log.warn("返金申請一致データなし。requestNo={}, refundNumber={}, orderNumber={}",
                    lookupRequestNo, refundNumber, orderNumber);
        }

        List<OrderDetailDto> result = cm901052Mapper.getOrderDetail(refundNumber, orderNumber);

        if (result == null) {
            result = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "交換/返金 照会", String.valueOf(result.size()) });

        return result;
    }

    /**
     * 返金処理の実行
     * 
     * @param requestNo 返金情報
     * @param userId    更新者
     * @return 返金処理の更新
     */
    @Override
    @Transactional
    public Integer updateRefund(RefundRequestDto requestDto, String userId) {
        int result = refund(requestDto, userId);
        result += refundDetail(requestDto, userId);
        return result;
    }

    /**
     * 返金更新
     * 
     * @param requestNo 返金情報
     * @param userId    更新者
     * @return 返金処理の更新
     */
    private Integer refund(RefundRequestDto requestDto, String userId) {
        ReturnsDto returnsDto = new ReturnsDto();
        returnsDto.setCustomerId(requestDto.getRefundNumber());
        returnsDto.setOrderNo(requestDto.getRequestNo());
        returnsDto.setReason(requestDto.getMemo());
        returnsDto.setTotalAmount(requestDto.getTotalAmount());
        returnsDto.setRefundAmount(requestDto.getProductTotalAmount());
        if ("0".equals(requestDto.getRefundType())) {
            returnsDto.setReturnStatusValue(2);
        } else {
            returnsDto.setReturnStatusValue(4);
        }
        if ("0".equals(requestDto.getShippingFee())) {
            returnsDto.setShippingFeeSellerAmount(requestDto.getShippingAmount());
        } else {
            returnsDto.setShippingFeeCustomerAmount(requestDto.getShippingAmount());
        }
        returnsDto.setUpdatedBy(userId);
        returnsDto.setUpdatedAt(dateCalculator.tokyoTime());

        return cm901052Mapper.updateRefund(returnsDto);
    }

    /**
     * 返金明細の作成
     * 
     * @param requestNo 返金情報
     * @param userId    更新者
     * @return 返金処理の作成
     */
    private Integer refundDetail(RefundRequestDto requestDto, String userId) {
        int result = 0;
        ReturnsDto refundInfo = cm901052Mapper.getRefund(requestDto.getRequestNo(), requestDto.getRefundNumber());

        for (RefundRequestDto.RefundProductDto item : requestDto.getProducts()) {
            ReturnDetailsDto detailsDto = new ReturnDetailsDto();
            detailsDto.setReturnId(refundInfo.getReturnId());
            detailsDto.setUnitPrice(item.getUnitPrice());
            detailsDto.setQuantity(item.getQuantity());
            detailsDto.setProductCode(item.getProductNumber());
            detailsDto.setCreatedBy(userId);
            detailsDto.setCreatedAt(dateCalculator.tokyoTime());
            detailsDto.setUpdatedBy(userId);
            detailsDto.setUpdatedAt(dateCalculator.tokyoTime());

            result += cm901052Mapper.updateRefundDetail(detailsDto);
        }

        return result;
    }
}
