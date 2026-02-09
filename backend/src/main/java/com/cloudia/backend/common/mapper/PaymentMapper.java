package com.cloudia.backend.common.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface PaymentMapper {
    /**
     * PG READY 후 Payment 레코드 상태 업데이트
     */
    int updatePaymentForReady(
            @Param("paymentId") String paymentId,
            @Param("tid") String tid,
            @Param("resultCode") String resultCode,
            @Param("resultMsg") String resultMsg
    );

    /**
     * 결제 승인(approve) 후 payment 상태 업데이트
     */
    int updatePaymentStatusOnApprove(
            @Param("tid") String tid,
            @Param("resultCode") String resultCode,
            @Param("resultMsg") String resultMsg,
            @Param("statusCode") Integer statusCode,  // 2: 승인 / 3: 실패
            @Param("approvedAt") LocalDateTime approvedAt
    );

    /**
     * 결제 취소(cancel) 후 payment 상태 업데이트
     */
    int updatePaymentStatusOnCancel(
            @Param("paymentId") String paymentId,
            @Param("resultCode") String resultCode,
            @Param("resultMsg") String resultMsg
    );

    /**
     * 승인 완료 후 주문 상태 업데이트
     */
    int updateOrderStatusOnApprove(
            @Param("orderId") Long orderId,
            @Param("orderStatusValue") Integer orderStatusValue, // 2: 구매확정, 7: 실패/취소
            @Param("statusMsg") String statusMsg
    );

    /**
     * 취소 후 주문 상태 업데이트
     */
    int updateOrderStatusOnCancel(
            @Param("orderId") Long orderId,
            @Param("orderStatusValue") Integer orderStatusValue // 7: 유저취소 / 6: 관리자취소
    );

    /**
     * 주문 최종 완료 처리
     */
    int updateOrderStatusToCompleted(
            @Param("orderId") Long orderId
    );
}
