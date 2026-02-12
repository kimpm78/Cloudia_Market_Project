package com.cloudia.backend.CM_90_1051.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1051.model.AddressDto;
import com.cloudia.backend.CM_90_1051.model.OrderDetailDto;
import com.cloudia.backend.CM_90_1051.model.OrderDto;
import com.cloudia.backend.CM_90_1051.model.SearchRequestDto;

@Mapper
public interface CM901051Mapper {
    /**
     * 注文一覧を取得
     *
     * @return 注文一覧
     */
    List<OrderDto> findByAllOrders();

    /**
     * 注文一覧を検索して取得
     *
     * @param searchRequest 検索条件
     * @return 注文一覧
     */
    List<OrderDto> getFindOrders(SearchRequestDto searchRequest);

    /**
     * 注文情報を検索して取得
     *
     * @param searchRequest 検索条件
     * @return 注文一覧
     */
    List<OrderDto> getFindOrder(SearchRequestDto searchRequest);

    /**
     * 注文詳細一覧を検索して取得
     *
     * @param searchRequest 検索条件
     * @return 注文詳細一覧
     */
    List<OrderDetailDto> getFindOrderDetail(SearchRequestDto searchRequest);

    /**
     * 精算ステータスを更新
     *
     * @param entity 更新対象
     * @return 更新件数
     */
    int uptStatus(OrderDto entity);

    /**
     * 配送先情報を取得
     *
     * @param searchRequest 検索条件
     * @return 配送先情報
     */
    AddressDto getAddress(SearchRequestDto searchRequest);

    /**
     * 注文IDの最新決済TIDを取得
     *
     * @param orderId 注文ID
     * @return 取引TID
     */
    String findLatestTransactionIdByOrderId(@Param("orderId") Integer orderId);

    /**
     * 注文IDの最新決済TIDを更新（未設定時のみ）
     *
     * @param orderId 注文ID
     * @param transactionId 取引TID
     * @param updatedBy 更新者
     * @return 更新件数
     */
    int updateLatestPaymentTransactionIdByOrderId(
            @Param("orderId") Integer orderId,
            @Param("transactionId") String transactionId,
            @Param("updatedBy") String updatedBy);

    /**
     * 決済レコードがない場合、ローカルカード決済モックを作成
     *
     * @param orderId 注文ID
     * @param transactionId 取引TID
     * @param updatedBy 更新者
     * @return 追加件数
     */
    int insertLocalMockCardPaymentIfMissing(
            @Param("orderId") Integer orderId,
            @Param("transactionId") String transactionId,
            @Param("updatedBy") String updatedBy);
}
