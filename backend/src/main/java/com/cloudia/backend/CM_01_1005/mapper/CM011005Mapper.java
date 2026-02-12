package com.cloudia.backend.CM_01_1005.mapper;

import com.cloudia.backend.CM_01_1005.model.OrderDetailResponse;
import com.cloudia.backend.CM_01_1005.model.OrderEntity;
import com.cloudia.backend.CM_06_1001.model.PaymentInfo;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface CM011005Mapper {

        /**
         * 注文履歴一覧の閲覧（フィルター適用）
         */
        List<OrderEntity> findOrderHistoryByFilters(Map<String, Object> params);

        /**
         * 特定の注文の基本詳細情報の照会
         */
        OrderEntity findOrderByOrderNoAndMemberNumber(
                        @Param("orderNo") String orderNo,
                        @Param("memberNumber") String memberNumber);

        /**
         * 特定の注文の決済および商品詳細の照会
         */
        List<OrderDetailResponse.Payment> findPaymentDetailsByOrderNo(
                        @Param("orderNo") String orderNo,
                        @Param("memberNumber") String memberNumber);

        /**
         * 返金申請情報の登録（口座振込返金など）
         */
        int insertRefundRequest(Map<String, Object> params);

        /**
         * 注文ステータスコードの変更（キャンセル、確定など）
         */
        int updateOrderStatus(
                        @Param("orderNo") String orderNo,
                        @Param("memberNumber") String memberNumber,
                        @Param("orderStatusValue") Integer orderStatusValue,
                        @Param("updatedAt") java.time.LocalDateTime updatedAt);

        /**
         * ユーザーの配送先住所一覧の照会
         */
        List<Map<String, Object>> findAddressesByMemberNumber(String memberNumber);

        /**
         * 返金申請情報の登録（口座振込返金など）
         */
        int updateOrderShippingInfo(Map<String, Object> params);

        /**
         * ユーザーの登録された返金口座情報の照会
         */
        OrderDetailResponse.UserRefundInfo findUserRefundInfoByMemberNumber(String memberNumber);

        /**
         * 商品コードによる在庫マスター情報の照会
         */
        Map<String, Object> findStockByProductCode(@Param("productCode") String productCode);

        /**
         * 在庫マスター 数量更新
         */
        int updateStockQty(
                        @Param("productCode") String productCode,
                        @Param("quantity") int quantity,
                        @Param("updatedAt") LocalDateTime updatedAt);

        /**
         * 在庫 変動 詳細履歴の保存
         */
        int insertStockDetail(
                        @Param("stockId") Long stockId,
                        @Param("quantity") int quantity,
                        @Param("reason") String reason,
                        @Param("memberNumber") String memberNumber,
                        @Param("createdAt") LocalDateTime createdAt);

        /**
         * 注文ID（PK）で決済情報を照会
         */
        PaymentInfo findPaymentInfoByOrderId(Long orderId);

        /**
         * 注文キャンセル時の在庫復旧のために、注文商品と現在の在庫情報を一度に確認できます
         */
        List<Map<String, Object>> findOrderItemsForStockRestore(@Param("orderNo") String orderNo);

        /**
         * 注文状況の変更（理由を含む）
         */
        int cancelOrderWithReason(@Param("orderNo") String orderNo,
                        @Param("memberNumber") String memberNumber,
                        @Param("reason") String reason,
                        @Param("updatedAt") LocalDateTime updatedAt);
}