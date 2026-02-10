package com.cloudia.backend.CM_90_1052.service;

import java.util.List;

import com.cloudia.backend.CM_90_1052.model.OrderDetailDto;
import com.cloudia.backend.CM_90_1052.model.RefundRequestDto;
import com.cloudia.backend.CM_90_1052.model.RefundSearchRequestDto;
import com.cloudia.backend.CM_90_1052.model.ReturnsDto;

public interface CM901052Service {
    /**
     * 返金/交換リスト取得
     * 
     * @return 返金/交換リスト
     */
    List<ReturnsDto> getRefund();

    /**
     * 返金/交換リスト取得
     * 
     * @return 返金/交換リスト
     */
    List<ReturnsDto> getPeriod(RefundSearchRequestDto searchDto);

    /**
     * 返金商品リスト
     * 
     * @param requestNo    リクエスト番号
     * @param refundNumber 社員番号
     * @param orderNumber  注文番号
     * @return 返金商品リスト
     */
    List<OrderDetailDto> getOrderDetail(String requestNo,
            String refundNumber,
            String orderNumber);

    /**
     * 返金処理の実行
     * 
     * @param requestDto 返金情報
     * @return 返金処理の更新
     */
    Integer updateRefund(RefundRequestDto requestDto, String userId);
}
