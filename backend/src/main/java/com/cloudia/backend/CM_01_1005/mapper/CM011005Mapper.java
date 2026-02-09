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
         * 주문 이력 목록 조회 (필터 적용)
         */
        List<OrderEntity> findOrderHistoryByFilters(Map<String, Object> params);

        /**
         * 특정 주문의 기본 상세 정보 조회
         */
        OrderEntity findOrderByOrderNoAndMemberNumber(
                        @Param("orderNo") String orderNo,
                        @Param("memberNumber") String memberNumber);

        /**
         * 특정 주문의 결제 및 상품 상세 내역 조회
         */
        List<OrderDetailResponse.Payment> findPaymentDetailsByOrderNo(
                        @Param("orderNo") String orderNo,
                        @Param("memberNumber") String memberNumber);

        /**
         * 환불 요청 정보 등록 (계좌이체 환불 등)
         */
        int insertRefundRequest(Map<String, Object> params);

        /**
         * 주문 상태 코드 변경 (취소, 확정 등)
         */
        int updateOrderStatus(
                        @Param("orderNo") String orderNo,
                        @Param("memberNumber") String memberNumber,
                        @Param("orderStatusValue") Integer orderStatusValue,
                        @Param("updatedAt") java.time.LocalDateTime updatedAt);

        /**
         * 사용자의 배송지 주소 목록 조회
         */
        List<Map<String, Object>> findAddressesByMemberNumber(String memberNumber);

        /**
         * 환불 요청 정보 등록 (계좌이체 환불 등)
         */
        int updateOrderShippingInfo(Map<String, Object> params);

        /**
         * 사용자의 등록된 환불 계좌 정보 조회
         */
        OrderDetailResponse.UserRefundInfo findUserRefundInfoByMemberNumber(String memberNumber);

        /**
         * 제품 코드로 재고 마스터 정보 조회
         */
        Map<String, Object> findStockByProductCode(@Param("productCode") String productCode);

        /**
         * 재고 마스터 수량 업데이트
         */
        int updateStockQty(
                        @Param("productCode") String productCode,
                        @Param("quantity") int quantity,
                        @Param("updatedAt") LocalDateTime updatedAt);

        /**
         * 재고 변동 상세 이력 저장
         */
        int insertStockDetail(
                        @Param("stockId") Long stockId,
                        @Param("quantity") int quantity,
                        @Param("reason") String reason,
                        @Param("memberNumber") String memberNumber,
                        @Param("createdAt") LocalDateTime createdAt);

        /**
         * 주문 ID(PK)로 결제 정보 조회
         */
        PaymentInfo findPaymentInfoByOrderId(Long orderId);

        /**
         * 주문 취소 시 재고 복구를 위해 주문 상품과 현재 재고 정보를 한 번에 조회
         */
        List<Map<String, Object>> findOrderItemsForStockRestore(@Param("orderNo") String orderNo);

        /**
         * 주문 상태 변경 (사유 포함)
         */
        int cancelOrderWithReason(@Param("orderNo") String orderNo,
                        @Param("memberNumber") String memberNumber,
                        @Param("reason") String reason,
                        @Param("updatedAt") LocalDateTime updatedAt);
}