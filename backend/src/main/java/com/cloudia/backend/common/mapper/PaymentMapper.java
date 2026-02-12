package com.cloudia.backend.common.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface PaymentMapper {
    /**
     * PG READY 後、Paymentレコードの状態を更新
     */
    int updatePaymentForReady(
            @Param("paymentId") String paymentId,
            @Param("tid") String tid,
            @Param("resultCode") String resultCode,
            @Param("resultMsg") String resultMsg
    );

    /**
     * 決済承認（approve）後、payment状態を更新
     */
    int updatePaymentStatusOnApprove(
            @Param("tid") String tid,
            @Param("resultCode") String resultCode,
            @Param("resultMsg") String resultMsg,
            @Param("statusCode") Integer statusCode,  // 2: 承認 / 3: 失敗
            @Param("approvedAt") LocalDateTime approvedAt
    );

    /**
     * 決済キャンセル（cancel）後、payment状態を更新
     */
    int updatePaymentStatusOnCancel(
            @Param("paymentId") String paymentId,
            @Param("resultCode") String resultCode,
            @Param("resultMsg") String resultMsg
    );

    /**
     * 承認完了後、注文状態を更新
     */
    int updateOrderStatusOnApprove(
            @Param("orderId") Long orderId,
            @Param("orderStatusValue") Integer orderStatusValue, // 2: 購入確定, 7: 失敗／キャンセル
            @Param("statusMsg") String statusMsg
    );

    /**
     * キャンセル後、注文状態を更新
     */
    int updateOrderStatusOnCancel(
            @Param("orderId") Long orderId,
            @Param("orderStatusValue") Integer orderStatusValue // 7: ユーザーキャンセル / 6: 管理者キャンセル
    );

    /**
     * 注文の最終完了処理
     */
    int updateOrderStatusToCompleted(
            @Param("orderId") Long orderId
    );
}
