package com.cloudia.backend.CM_01_1005.service;

import com.cloudia.backend.CM_01_1005.model.OrderDetailResponse;
import com.cloudia.backend.CM_01_1005.model.OrderListResponse;
import com.cloudia.backend.CM_01_1005.model.OrderCancelRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface CM011005Service {

    /**
     * ログインIDと検索条件に一致する注文履歴を取得
     */
    ResponseEntity<List<OrderListResponse>> searchOrderHistory(String loginId, Map<String, Object> filters);

    /**
     * 特定注文の詳細情報を取得
     */
    ResponseEntity<OrderDetailResponse> getOrderDetail(String loginId, String orderNo);

    /**
     * 注文キャンセル申請（クレジットカード取消または口座返金申請）
     */
    ResponseEntity<String> cancelOrder(String loginId, OrderCancelRequest request);

    /**
     * ユーザーの配送先一覧を取得（配送先変更モーダル用）
     */
    ResponseEntity<List<Map<String, Object>>> getDeliveryAddresses(String loginId);

    /**
     * 注文の配送先情報を更新（購入確定前のみ可能）
     */
    ResponseEntity<String> updateShippingInfo(String loginId, Map<String, Object> params);
}