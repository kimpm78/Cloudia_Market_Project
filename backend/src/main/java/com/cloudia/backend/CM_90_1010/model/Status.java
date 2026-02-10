package com.cloudia.backend.CM_90_1010.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Status {
    private Integer newOrder;           // 新規注文
    private Integer cancelRequested;    // キャンセル確定
    private Integer reserved;           // 予約確定
    private Integer paymentPending;     // 入金待ち
    private Integer answerPending;      // 回答待ち
    private Integer answerCompleted;    // 回答完了
    private Integer exchangeInProgress; // 交換処理中
    private Integer exchangeCompleted;  // 交換完了
    private Integer refundInProgress;   // 返金処理中
    private Integer refundCompleted;    // 返金完了
    private Integer purchaseConfirmed;  // 購入確定
    private Integer preparingShipment;  // 出荷準備中
    private Integer inTransit;          // 配送中
    private Integer delivered;          // 配送完了
    private Integer exchangeRequested;  // 交換依頼
    private Integer refundRequested;    // 返金依頼
}
