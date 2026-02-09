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
import java.time.LocalDateTime;
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
            log.warn("상태 코드 변환 실패: code={}, val={}", "008", entity.getOrderStatusValue());
            statusName = entity.getOrderStatus() != null ? entity.getOrderStatus() : "";
        }

        String deliveryDate = entity.getDeliveryDate();
        if (deliveryDate != null && deliveryDate.contains("-")) {
            String[] dateParts = deliveryDate.split("-");
            if (dateParts.length >= 2)
                deliveryDate = dateParts[0] + "년 " + dateParts[1] + "월";
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
     * 주문 이력 목록 조회
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
     * 특정 주문의 상세 내역 조회
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

                detail.setTax(nf.format(currentTotal / 11) + "원");
                detail.setTotal(nf.format(currentTotal) + "원");

                // 배송비 처리
                if (i == 0) {
                    long cost = (orderEntity.getShippingCost() != null) ? orderEntity.getShippingCost() : 0L;
                    detail.setShipping(cost <= 0 ? "무료 배송" : nf.format(cost) + "원");
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

            // 배송 정보
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
            log.error("주문 상세 조회 에러: orderNo={}", orderNo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 주문 취소 요청
     */
    @Override
    @Transactional
    public ResponseEntity<String> cancelOrder(String loginId, OrderCancelRequest request) {
        log.info("요청자: {}, 주문번호: {}, 사유: {}", loginId, request.getOrderNo(), request.getReason());

        User user = userMapper.findByLoginId(loginId);
        if (user == null) {
            log.error("사용자 인증 실패: {}", loginId);
            return ResponseEntity.badRequest().body("사용자 인증 실패");
        }

        OrderEntity order = orderMapper.findOrderByOrderNoAndMemberNumber(request.getOrderNo(), user.getMemberNumber());
        if (order == null) {
            log.error("주문 미존재 또는 권한 없음: OrderNo={}", request.getOrderNo());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("주문을 찾을 수 없습니다.");
        }

        // 송금확인중(무통장) 취소는 사유를 저장/전송하지 않음
        final String cancelReason = (order.getPaymentValue() == 1)
                ? null
                : request.getReason();

        // PG 결제 취소 처리 (카드 결제인 경우)
        if (order.getPaymentValue() == 2) {
            log.info("카드 결제 취소 시도: OrderNo={}", request.getOrderNo());
            PaymentInfo paymentInfo = orderMapper.findPaymentInfoByOrderId(order.getOrderId());

            if (paymentInfo != null) {
                if (!"2".equals(paymentInfo.getPaymentStatusCode())) {
                    log.warn("결제 상태가 승인이 아니므로 PG 취소 API 호출 생략: 상태코드={}, OrderNo={}",
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
                            log.error("PG사 거절: {}", pgResponse.getMessage());
                            return ResponseEntity.badRequest().body("카드 결제 취소 실패: " + pgResponse.getMessage());
                        }
                        log.info("PG사 취소 완료: TID={}", paymentInfo.getTransactionId());
                    } catch (Exception e) {
                        log.error("PG 취소 중 예외 발생", e);
                        throw new RuntimeException("PG 취소 실패로 인한 트랜잭션 롤백");
                    }
                }
            } else {
                log.warn("DB에 결제 정보가 없음. OrderNo={}", request.getOrderNo());
            }
        }

        List<Map<String, Object>> restoreItems = orderMapper.findOrderItemsForStockRestore(request.getOrderNo());
        log.info("복구 대상 상품 개수: {}개", restoreItems.size());

        // 주문 상태 변경
        int updateCount = orderMapper.cancelOrderWithReason(request.getOrderNo(), user.getMemberNumber(),
                cancelReason, dateCalculator.tokyoTime());
        if (updateCount > 0) {
            log.info("주문 상태 변경 완료: '취소', 사유 저장 완료");
        } else {
            log.error("주문 상태 변경 실패: OrderNo={}", request.getOrderNo());
            throw new RuntimeException("주문 상태 변경 실패");
        }

        // 재고 복구 로직
        for (Map<String, Object> item : restoreItems) {
            String productCode = (String) item.get("productCode");
            int quantity = Integer.parseInt(String.valueOf(item.get("quantity")));

            if (item.get("stockId") != null) {
                Long stockId = Long.parseLong(String.valueOf(item.get("stockId")));

                log.debug("상품 {}의 복구 전 재고: {}", productCode, item.get("currentStock"));

                // 재고 수량 증가
                orderMapper.updateStockQty(productCode, quantity, dateCalculator.tokyoTime());

                // 재고 변동 이력 기록
                String reasonLog = "주문 취소 - 주문번호: " + request.getOrderNo();
                orderMapper.insertStockDetail(stockId, quantity, reasonLog, user.getMemberNumber(),
                        dateCalculator.tokyoTime());

                log.info("재고 복구 및 이력 기록 완료: 상품코드={}, 복구수량={}", productCode, quantity);
            } else {
                log.warn("재고 정보(stocks)가 없는 상품입니다: 상품코드={}", productCode);
            }
        }

        log.info("주문번호: {}", request.getOrderNo());
        return ResponseEntity.ok("주문이 취소되었으며, 상품 재고가 정상적으로 복구되었습니다.");
    }

    /**
     * 사용자의 배송지 목록 조회 (배송지 변경 모달용)
     */
    @Override
    public ResponseEntity<List<Map<String, Object>>> getDeliveryAddresses(String loginId) {
        User user = userMapper.findByLoginId(loginId);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(orderMapper.findAddressesByMemberNumber(user.getMemberNumber()));
    }

    /**
     * 주문 배송지 정보 수정
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
        return result > 0 ? ResponseEntity.ok("배송지가 변경되었습니다.") : ResponseEntity.badRequest().body("변경 실패");
    }
}