package com.cloudia.backend.CM_01_1005.service.impl;

import com.cloudia.backend.CM_01_1001.mapper.CM011001UserMapper;
import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_01_1005.constants.CM011005MessageConstant;
import com.cloudia.backend.CM_01_1005.mapper.CM011005Mapper;
import com.cloudia.backend.CM_01_1005.model.OrderDetailResponse;
import com.cloudia.backend.CM_01_1005.model.OrderEntity;
import com.cloudia.backend.CM_01_1005.model.OrderListResponse;
import com.cloudia.backend.CM_01_1005.model.OrderCancelRequest;
import com.cloudia.backend.CM_01_1005.service.CM011005Service;
import com.cloudia.backend.CM_06_1001.model.PaymentInfo;
import com.cloudia.backend.common.model.CodeMaster;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.model.pg.PGCancelRequest;
import com.cloudia.backend.common.service.CodeMasterService;
import com.cloudia.backend.common.service.PaymentService;
import com.cloudia.backend.common.util.DateCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CM011005ServiceImpl implements CM011005Service {

    private final CM011005Mapper orderMapper;
    private final CM011001UserMapper userMapper;
    private final CodeMasterService codeMasterService;
    private final DateCalculator dateCalculator;
    private final PaymentService paymentService;

    private OrderListResponse convertToOrderListResponse(OrderEntity entity) {
        String statusName = "";
        try {
            CodeMaster statusMaster = codeMasterService.getCodeByValue("008", entity.getOrderStatusValue());
            if (statusMaster != null) {
                statusName = statusMaster.getCodeValueName();
            }
        } catch (Exception e) {
            log.warn("ステータスコード変換失敗: code={}, val={}", "008", entity.getOrderStatusValue());
            statusName = entity.getOrderStatus() != null ? entity.getOrderStatus() : "";
        }

        String deliveryDate = entity.getDeliveryDate();
        if (deliveryDate != null && deliveryDate.contains("-")) {
            String[] dateParts = deliveryDate.split("-");
            if (dateParts.length >= 2)
                deliveryDate = dateParts[0] + "年 " + dateParts[1] + "月";
        }

        String orderDate = (entity.getOrderDate() != null)
                ? entity.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                : "";

        long totalAmount = (entity.getTotalAmount() != null) ? entity.getTotalAmount() : 0L;
        long shippingCost = (entity.getShippingCost() != null) ? entity.getShippingCost() : 0L;
        long totalPrice = totalAmount + shippingCost;

        return OrderListResponse.builder()
                .orderNo(entity.getOrderNo())
                .orderDate(orderDate)
                .productName(entity.getProductName())
                .deliveryDate(deliveryDate)
                .totalPrice(totalPrice)
                .paymentValue(entity.getPaymentValue())
                .paymentAt(entity.getPaymentAt())
                .orderStatus(statusName)
                .orderStatusValue(entity.getOrderStatusValue())
                .productImageUrl(entity.getProductImageUrl())
                .build();
    }

    /**
     * 配送情報の取得
     */
    @Override
    public ResponseEntity<List<OrderListResponse>> searchOrderHistory(String loginId, Map<String, Object> filters) {
        log.info(CM011005MessageConstant.SERVICE_SEARCH_HISTORY, loginId);

        User user = userMapper.findByLoginId(loginId);
        if (user == null)
            return ResponseEntity.ok(List.of());

        filters.put("memberNumber", user.getMemberNumber());
        List<OrderEntity> list = orderMapper.findOrderHistoryByFilters(filters);

        return ResponseEntity.ok(list.stream()
                .map(this::convertToOrderListResponse)
                .collect(Collectors.toList()));
    }

    /**
     * 配送情報の取得
     */
    @Override
    public ResponseEntity<OrderDetailResponse> getOrderDetail(String loginId, String orderNo) {
        try {
            User user = userMapper.findByLoginId(loginId);
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            OrderEntity orderEntity = orderMapper.findOrderByOrderNoAndMemberNumber(orderNo, user.getMemberNumber());
            if (orderEntity == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            List<OrderDetailResponse.Payment> paymentDetails = orderMapper.findPaymentDetailsByOrderNo(orderNo,
                    user.getMemberNumber());
            NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);

            for (int i = 0; i < paymentDetails.size(); i++) {
                OrderDetailResponse.Payment detail = paymentDetails.get(i);

                String totalStr = detail.getTotal() != null ? detail.getTotal().replaceAll("[^0-9]", "") : "0";
                long currentTotal = totalStr.isEmpty() ? 0 : Long.parseLong(totalStr);

                detail.setTax(nf.format(currentTotal / 11) + "円");
                detail.setTotal(nf.format(currentTotal) + "円");

                // 配送費の設定
                if (i == 0) {
                    long cost = (orderEntity.getShippingCost() != null) ? orderEntity.getShippingCost() : 0L;
                    detail.setShipping(cost <= 0 ? "無料配送" : nf.format(cost) + "円");
                } else {
                    detail.setShipping("-");
                }
            }

            String statusName = "";
            try {
                CodeMaster sm = codeMasterService.getCodeByValue("008", orderEntity.getOrderStatusValue());
                if (sm != null) {
                    statusName = sm.getCodeValueName();
                } else {
                    statusName = orderEntity.getOrderStatus();
                }
            } catch (Exception e) {
                statusName = orderEntity.getOrderStatus();
            }

            String orderDateFmt = (orderEntity.getOrderDate() != null)
                    ? orderEntity.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                    : "";

            long shipCost = (orderEntity.getShippingCost() != null) ? orderEntity.getShippingCost() : 0L;

            OrderDetailResponse.UserRefundInfo refundInfo = orderMapper
                    .findUserRefundInfoByMemberNumber(user.getMemberNumber());

            OrderDetailResponse.Summary summary = OrderDetailResponse.Summary.builder()
                    .orderNo(orderEntity.getOrderNo())
                    .orderDate(orderDateFmt)
                    .orderStatus(statusName)
                    .orderStatusValue(orderEntity.getOrderStatusValue())
                    .paymentValue(orderEntity.getPaymentValue())
                    .productName(orderEntity.getProductName())
                    .shippingCost(shipCost)
                    .paymentAt(orderEntity.getPaymentAt())
                    .userRefundInfo(refundInfo)
                    .build();

            // 配送情報
            OrderDetailResponse.Shipping shipping = OrderDetailResponse.Shipping.builder()
                    .address(orderEntity.getShippingAddress() != null ? orderEntity.getShippingAddress() : "")
                    .receiver(orderEntity.getRecipientName() != null ? orderEntity.getRecipientName() : "")
                    .phone(orderEntity.getRecipientPhone() != null ? orderEntity.getRecipientPhone() : "")
                    .tracking(orderEntity.getTrackingNumber() != null ? orderEntity.getTrackingNumber() : "")
                    .build();

            return ResponseEntity.ok(OrderDetailResponse.builder()
                    .orderSummary(summary)
                    .paymentDetails(paymentDetails)
                    .shippingInfo(shipping)
                    .userRefundInfo(refundInfo)
                    .build());

        } catch (Exception e) {
            log.error("注文詳細取得エラー: orderNo={}", orderNo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 注文キャンセル要求
     */
    @Override
    @Transactional
    public ResponseEntity<String> cancelOrder(String loginId, OrderCancelRequest request) {
        log.info("依頼者: {}, 注文番号: {}, 理由: {}", loginId, request.getOrderNo(), request.getReason());

        User user = userMapper.findByLoginId(loginId);
        if (user == null) {
            log.error("ユーザー認証失敗: {}", loginId);
            return ResponseEntity.badRequest().body("ユーザー認証に失敗しました。");
        }

        OrderEntity order = orderMapper.findOrderByOrderNoAndMemberNumber(request.getOrderNo(), user.getMemberNumber());
        if (order == null) {
            log.error("注文が存在しない、または権限がありません: OrderNo={}", request.getOrderNo());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("注文が見つかりません。");
        }

        // 入金確認中（銀行振込）のキャンセルは理由を保存／送信しない
        final String cancelReason = (order.getPaymentValue() == 1)
                ? null
                : request.getReason();

        // PG決済キャンセル処理（カード決済の場合）
        if (order.getPaymentValue() == 2) {
            log.info("カード決済のキャンセルを試行: OrderNo={}", request.getOrderNo());
            PaymentInfo paymentInfo = orderMapper.findPaymentInfoByOrderId(order.getOrderId());

            if (paymentInfo != null) {
                if (!"2".equals(paymentInfo.getPaymentStatusCode())) {
                    log.warn("決済ステータスが承認ではないためPGキャンセルAPI呼び出しをスキップ: ステータスコード={}, OrderNo={}",
                            paymentInfo.getPaymentStatusCode(), request.getOrderNo());
                } else if (paymentInfo.getTransactionId() != null) {
                    PGCancelRequest pgCancelRequest = new PGCancelRequest();
                    pgCancelRequest.setTid(paymentInfo.getTransactionId());
                    pgCancelRequest.setAmount(paymentInfo.getAmount());
                    pgCancelRequest.setReason(cancelReason);
                    pgCancelRequest.setPgType(paymentInfo.getPgProvider());
                    pgCancelRequest.setOrderId(order.getOrderId());
                    pgCancelRequest.setOrderNumber(order.getOrderNo());
                    pgCancelRequest.setPaymentId(paymentInfo.getPaymentId());

                    try {
                        ResponseModel<Map<String, Object>> pgResponse = paymentService.cancel(pgCancelRequest);

                        if (!pgResponse.isResult()) {
                            log.error("PG側で拒否されました: {}", pgResponse.getMessage());
                            return ResponseEntity.badRequest().body("カード決済のキャンセルに失敗しました: " + pgResponse.getMessage());
                        }
                        log.info("PGキャンセル完了: TID={}", paymentInfo.getTransactionId());
                    } catch (Exception e) {
                        log.error("PGキャンセル中に例外が発生", e);
                        throw new RuntimeException("PGキャンセル失敗のためトランザクションをロールバック");
                    }
                }
            } else {
                log.warn("DBに決済情報がありません。OrderNo={}", request.getOrderNo());
            }
        }

        List<Map<String, Object>> restoreItems = orderMapper.findOrderItemsForStockRestore(request.getOrderNo());
        log.info("復元対象の商品数: {}件", restoreItems.size());

        // 注文ステータス変更
        int updateCount = orderMapper.cancelOrderWithReason(request.getOrderNo(), user.getMemberNumber(),
                cancelReason, dateCalculator.tokyoTime());
        if (updateCount > 0) {
            log.info("注文ステータス変更完了: 'キャンセル'、理由の保存完了");
        } else {
            log.error("注文ステータス変更失敗: OrderNo={}", request.getOrderNo());
            throw new RuntimeException("注文ステータス変更に失敗しました");
        }

        // 在庫復元ロジック
        for (Map<String, Object> item : restoreItems) {
            String productCode = (String) item.get("productCode");
            int quantity = Integer.parseInt(String.valueOf(item.get("quantity")));

            if (item.get("stockId") != null) {
                Long stockId = Long.parseLong(String.valueOf(item.get("stockId")));

                log.debug("商品 {} の復元前在庫: {}", productCode, item.get("currentStock"));

                // 在庫数量を増加
                orderMapper.updateStockQty(productCode, quantity, dateCalculator.tokyoTime());

                // 在庫変動履歴を記録
                String reasonLog = "注文キャンセル - 注文番号: " + request.getOrderNo();
                orderMapper.insertStockDetail(stockId, quantity, reasonLog, user.getMemberNumber(),
                        dateCalculator.tokyoTime());

                log.info("在庫復元および履歴記録完了: 商品コード={}, 復元数量={}", productCode, quantity);
            } else {
                log.warn("在庫情報（stocks）がない商品です: 商品コード={}", productCode);
            }
        }

        log.info("注文番号: {}", request.getOrderNo());
        return ResponseEntity.ok("注文をキャンセルし、商品の在庫を正常に復元しました。");
    }

    /**
     * ユーザーの配送先一覧取得（配送先変更モーダル用）
     */
    @Override
    public ResponseEntity<List<Map<String, Object>>> getDeliveryAddresses(String loginId) {
        User user = userMapper.findByLoginId(loginId);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(orderMapper.findAddressesByMemberNumber(user.getMemberNumber()));
    }

    /**
     * 注文の配送先情報更新
     */
    @Override
    @Transactional
    public ResponseEntity<String> updateShippingInfo(String loginId, Map<String, Object> params) {
        User user = userMapper.findByLoginId(loginId);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        params.put("memberNumber", user.getMemberNumber());
        params.put("updatedAt", dateCalculator.tokyoTime());

        int result = orderMapper.updateOrderShippingInfo(params);
        return result > 0 ? ResponseEntity.ok("配送先を変更しました。") : ResponseEntity.badRequest().body("変更に失敗しました。");
    }
}