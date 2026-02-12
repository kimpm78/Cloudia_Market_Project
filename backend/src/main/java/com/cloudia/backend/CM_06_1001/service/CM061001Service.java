package com.cloudia.backend.CM_06_1001.service;

import com.cloudia.backend.CM_06_1001.model.OrderCreate;
import com.cloudia.backend.CM_06_1001.model.OrderSummary;
import com.cloudia.backend.CM_06_1001.model.PaymentInfo;

public interface CM061001Service {

    /**
     * 注文作成
     *
     * @param request 注文作成リクエスト情報
     * @return 作成された注文サマリー情報
     */
    OrderSummary createOrder(OrderCreate request);

    /**
     * 注文完了処理
     *
     * @param orderId 注文ID
     */
    void completeOrder(Long orderId, String memberNumber);

    /**
     * 注文サマリー情報を取得
     *
     * @param orderId      注文ID
     * @param memberNumber 会員番号（権限検証用）
     * @return 注文サマリー情報
     */
    OrderSummary getOrderSummary(Long orderId, String memberNumber);

    /**
     * 最新の決済ステータスを取得（orderNumber基準）
     *
     * @param orderNumber 注文番号
     * @return 最新の決済情報
     */
    PaymentInfo findLatestPayment(String orderNumber);

    /**
     * ローカルカード決済の完了処理（開発用）
     *
     * @param orderId 注文ID
     * @param memberNumber 会員番号
     * @return 注文サマリー
     */
    OrderSummary completeLocalCardPayment(Long orderId, String memberNumber);
}
