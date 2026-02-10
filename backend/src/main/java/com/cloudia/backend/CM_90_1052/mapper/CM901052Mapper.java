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
     * 返金・交換一覧取得
     * 
     * @return 返金・交換一覧
     */
    List<ReturnsDto> getRefunds();

    /**
     * 返金・交換一覧取得
     * 
     * @return 返金・交換一覧
     */
    List<ReturnsDto> getPeriod(RefundSearchRequestDto searchDto);

    /**
     * リクエスト番号取得
     * 
     * @param requestNo    リクエスト番号
     * @param refundNumber 社員番号
     * @return リクエスト番号有無
     */
    int getRefundCount(@Param("requestNo") String requestNo,
            @Param("refundNumber") String refundNumber);

    /**
     * リクエスト番号取得
     * 
     * @param requestNo    リクエスト番号
     * @param refundNumber 社員番号
     * @return リクエスト番号有無
     */
    ReturnsDto getRefund(@Param("requestNo") String requestNo,
            @Param("refundNumber") String refundNumber);

    /**
     * 返金・返金商品一覧
     * 
     * @param refundNumber 社員番号
     * @param orderNumber  注文番号
     * @return 返金・返金商品一覧
     */
    List<OrderDetailDto> getOrderDetail(@Param("refundNumber") String refundNumber,
            @Param("orderNumber") String orderNumber);

    /**
     * 返金更新
     * 
     * @param requestDto 返金情報
     * @return 返金処理更新
     */
    int updateRefund(ReturnsDto requestDto);

    /**
     * 返金詳細作成
     * 
     * @param requestDto 返金情報
     * @return 返金処理作成
     */
    int updateRefundDetail(ReturnDetailsDto requestDto);
}
