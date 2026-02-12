package com.cloudia.backend.CM_06_1001.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_06_1001.constants.CM061001MessageConstant;
import com.cloudia.backend.CM_06_1001.mapper.CM061001Mapper;
import com.cloudia.backend.CM_06_1001.model.OrderCreate;
import com.cloudia.backend.CM_06_1001.model.OrderInfo;
import com.cloudia.backend.CM_06_1001.model.OrderItemInfo;
import com.cloudia.backend.CM_06_1001.model.OrderSummary;
import com.cloudia.backend.CM_06_1001.model.PaymentInfo;
import com.cloudia.backend.CM_06_1001.model.ShippingInfo;
import com.cloudia.backend.CM_06_1001.service.CM061001Service;
import com.cloudia.backend.CM_06_1000.model.CartItemResponse;
import com.cloudia.backend.common.model.EmailDto;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.service.EmailService;
import com.cloudia.backend.common.util.DateCalculator;
import com.cloudia.backend.constants.CMMessageConstant;


@Service
@RequiredArgsConstructor
@Slf4j
public class CM061001ServiceImpl implements CM061001Service {

    private final CM061001Mapper cm061001Mapper;
    private final EmailService emailService;
    private final DateCalculator dateCalculator;

    /**
     * 注文作成
     * @param request 注文作成リクエストオブジェクト
     * @return 生成された注文の概要情報
     */
    @Override
    @Transactional
    public OrderSummary createOrder(OrderCreate request) {
        log.info("注文生成: request={}", request);

        validateCreateOrderRequest(request);

        final ShippingInfo shippingInfo = request.getShipping();
        final List<Long> cartItemIds = request.getCartItemIds();
        final List<CartItemResponse> cartItems =
                cm061001Mapper.selectCartItemsForOrder(request.getUserId(), cartItemIds);
        if (cartItems == null || cartItems.isEmpty() || cartItems.size() != cartItemIds.size()) {
            throw new IllegalArgumentException(CM061001MessageConstant.CART_ITEMS_NOT_FOUND);
        }

        final String orderNumber = cm061001Mapper.selectNextOrderNumberByMember(request.getMemberNumber());
        final String paymentOrderNumber = cm061001Mapper.selectNextOrderNumber();
        final int shippingCost = request.getShippingFee() != null ? request.getShippingFee() : 0;
        // 合計金額はカートスナップショットを基準に算出（クライアント値は信用しない）
        final int subtotal =
                cartItems.stream()
                        .mapToInt(item -> item.getLineTotal() != null ? item.getLineTotal() : 0)
                        .sum();
        final int totalAmount = subtotal + shippingCost;

        // 受取人/連絡先: 配送情報がない場合は最低限 memberNumber を氏名として設定
        final String recipientName =
                shippingInfo != null && shippingInfo.getRecipientName() != null
                        ? shippingInfo.getRecipientName()
                        : request.getMemberNumber();
        final String recipientPhone =
                shippingInfo != null && shippingInfo.getRecipientPhone() != null
                        ? shippingInfo.getRecipientPhone()
                        : "";

        final int paymentValue = resolvePaymentValue(request.getPaymentMethod());

        final String userId =
                (request.getCreatedBy() != null && !request.getCreatedBy().isBlank())
                        ? request.getCreatedBy()
                        : request.getMemberNumber();

        final Long shippingAddressId;
        if (shippingInfo != null && shippingInfo.getAddressId() != null) {
            boolean ownsAddress =
                    cm061001Mapper.existsActiveDeliveryAddress(request.getMemberNumber(), shippingInfo.getAddressId());
            if (!ownsAddress) {
                throw new IllegalArgumentException(CM061001MessageConstant.SHIPPING_ADDRESS_INVALID);
            }
            shippingAddressId = shippingInfo.getAddressId();
        } else {
            shippingAddressId = null;
        }

        // 注文情報の作成
        final OrderInfo order =
                OrderInfo.builder()
                        .memberNumber(request.getMemberNumber())
                        .orderNumber(orderNumber)
                        .totalAmount(totalAmount)
                        .shippingCost(shippingCost)
                        .discountAmount(0L)
                        .paymentType("011")
                        .paymentValue(paymentValue)
                        .orderStatusType("008")
                        .orderStatusValue(1)
                        .shippingAddressId(shippingAddressId)
                        .recipientName(recipientName)
                        .recipientPhone(recipientPhone)
                        .createdBy(userId)
                        .createdAt(dateCalculator.tokyoTime())
                        .updatedBy(userId)
                        .updatedAt(dateCalculator.tokyoTime())
                        .build();
        cm061001Mapper.createOrder(order);
        final Long orderId = order.getOrderId();

        for (CartItemResponse cartItem : cartItems) {
            final int quantity = cartItem.getQuantity() != null ? cartItem.getQuantity() : 0;
            if (quantity < 1) {
                throw new IllegalStateException(CM061001MessageConstant.ORDER_QUANTITY_INVALID);
            }

            final Integer unitPrice = cartItem.getProductPrice();
            if (unitPrice == null || unitPrice < 0) {
                throw new IllegalStateException(CM061001MessageConstant.PRODUCT_PRICE_INVALID);
            }

            final int lineTotal = unitPrice * quantity;
            final OrderItemInfo orderItem =
                    OrderItemInfo.builder()
                            .orderId(orderId)
                            .orderNumber(orderNumber)
                            .memberNumber(request.getMemberNumber())
                            .productId(cartItem.getProductId())
                            .price(unitPrice)
                            .quantity(quantity)
                            .lineTotal(lineTotal)
                            .productName(cartItem.getProductName())
                            .imageLink(cartItem.getImageLink())
                            .createdBy(userId)
                            .updatedBy(userId)
                            .createdAt(dateCalculator.tokyoTime())
                            .updatedAt(dateCalculator.tokyoTime())
                            .build();
            cm061001Mapper.insertOrderItem(orderItem);
        }

        // 銀行（口座振替）決済処理
        if (paymentValue == 1) {
            final PaymentInfo payment =
                    PaymentInfo.builder()
                            .paymentId(UUID.randomUUID().toString())
                            .orderId(orderId)
                            .orderNumber(paymentOrderNumber)
                            .paymentType("011")
                            .paymentMethod("BANK")
                            .paymentStatusType("013")
                            .paymentStatusCode("1")
                            .amount(totalAmount)
                            .transactionId(null)
                            .createdBy(userId)
                            .updatedBy(userId)
                            .build();
            cm061001Mapper.insertPayment(payment);

            cm061001Mapper.deactivateCartItems(cartItemIds, userId);

            for (CartItemResponse cartItem : cartItems) {
                final int quantity = cartItem.getQuantity() != null ? cartItem.getQuantity() : 0;
                final int affected = cm061001Mapper.decreaseStock(cartItem.getProductId(), quantity);
                if (affected != 1) {
                    throw new IllegalStateException(CM061001MessageConstant.STOCK_NOT_ENOUGH);
                }
                final Long stockId = cm061001Mapper.findStockIdByProductCode(cartItem.getProductId());
                if (stockId == null) {
                    throw new IllegalStateException(CM061001MessageConstant.STOCK_NOT_FOUND);
                }
                cm061001Mapper.insertStockDetail(
                        stockId,
                        -1L * quantity,
                        null,
                        userId,
                        userId);
            }

            // 口座振替メール送信（メール失敗が注文作成や在庫処理の流れを妨げないようにログのみ残します）
            sendBankTransferGuideEmail(orderId, orderNumber, request.getMemberNumber(), recipientName);
        }

        log.info(CM061001MessageConstant.LOG_ORDER_CREATED, orderId, orderNumber);

        return cm061001Mapper.findOrderSummary(orderId);
    }

    /**
     * 注文の概要確認＋本人確認
     * @param orderId 注文 ID
     * @param memberNumber 会員番号
     * @return 注文の概要情報
     */
    @Override
    @Transactional(readOnly = true)
    public OrderSummary getOrderSummary(Long orderId, String memberNumber) {
        validateOrderAccessInput(orderId, memberNumber);

        final String owner = cm061001Mapper.findOrderOwner(orderId);
        if (owner == null || !owner.equals(memberNumber)) {
            log.warn(CM061001MessageConstant.LOG_ORDER_SUMMARY_ACCESS_DENIED, orderId, memberNumber);
            throw new AccessDeniedException("該当の注文にアクセスできません。");
        }

        final OrderSummary summary = cm061001Mapper.findOrderSummary(orderId);
        if (summary == null) {
            throw new IllegalStateException(CM061001MessageConstant.ORDER_NOT_FOUND);
        }

        final List<OrderItemInfo> items = cm061001Mapper.findOrderItems(orderId);
        summary.setItems(items);

        return summary;
    }

    /**
     * 最新決済情報の取得
     * @param orderNumber 注文番号
     * @return 最新の決済情報オブジェクト
     */
    @Override
    @Transactional(readOnly = true)
    public PaymentInfo findLatestPayment(String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new IllegalArgumentException(CM061001MessageConstant.ORDER_NUMBER_REQUIRED);
        }
        log.info(CM061001MessageConstant.LOG_LATEST_PAYMENT, orderNumber);
        return cm061001Mapper.findLatestPayment(orderNumber);
    }

    @Override
    @Transactional
    public OrderSummary completeLocalCardPayment(Long orderId, String memberNumber) {
        validateOrderAccessInput(orderId, memberNumber);

        final String owner = cm061001Mapper.findOrderOwner(orderId);
        if (owner == null || !memberNumber.equals(owner)) {
            log.warn(CM061001MessageConstant.LOG_ORDER_SUMMARY_ACCESS_DENIED, orderId, memberNumber);
            throw new AccessDeniedException("해당 주문에 접근할 수 없습니다.");
        }

        final OrderSummary summary = cm061001Mapper.findOrderSummary(orderId);
        if (summary == null) {
            throw new IllegalStateException(CM061001MessageConstant.ORDER_NOT_FOUND);
        }

        final String actor = resolveAuditUser(orderId, memberNumber);
        final List<OrderItemInfo> orderItems = cm061001Mapper.findOrderItems(orderId);
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalStateException(CM061001MessageConstant.CART_ITEMS_NOT_FOUND);
        }

        PaymentInfo payment = cm061001Mapper.findLatestPaymentByOrderId(orderId);
        boolean alreadyApproved = payment != null && "2".equals(String.valueOf(payment.getPaymentStatusCode()));

        if (!alreadyApproved) {
            if (payment == null) {
                final String paymentOrderNumber = cm061001Mapper.selectNextOrderNumber();
                final PaymentInfo localPayment = PaymentInfo.builder()
                        .paymentId(UUID.randomUUID().toString())
                        .orderId(orderId)
                        .orderNumber(paymentOrderNumber)
                        .paymentType("011")
                        .paymentMethod("CARD")
                        .paymentStatusType("013")
                        .paymentStatusCode("2")
                        .amount(summary.getTotalAmount() != null ? summary.getTotalAmount().intValue() : 0)
                        .transactionId(generateMockTid())
                        .approvedAt(dateCalculator.tokyoTime())
                        .createdBy(actor)
                        .updatedBy(actor)
                        .build();
                cm061001Mapper.insertPayment(localPayment);
                payment = cm061001Mapper.findLatestPaymentByOrderId(orderId);
            } else {
                cm061001Mapper.updatePaymentStatusOnApprove(
                        payment.getPaymentId(),
                        orderId,
                        payment.getTransactionId() != null ? payment.getTransactionId() : generateMockTid(),
                        "LOCAL_OK",
                        "Local card mock approved",
                        "2",
                        dateCalculator.tokyoTime().toString(),
                        actor);
            }

            cm061001Mapper.deactivateCartItemsByOrderId(orderId, actor);

            for (OrderItemInfo item : orderItems) {
                final String productId = item.getProductId();
                final int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                if (productId == null || productId.isBlank() || quantity < 1) {
                    throw new IllegalStateException(CM061001MessageConstant.ORDER_QUANTITY_INVALID);
                }

                final int affected = cm061001Mapper.decreaseStock(productId, quantity);
                if (affected != 1) {
                    throw new IllegalStateException(CM061001MessageConstant.STOCK_NOT_ENOUGH);
                }

                final Long stockId = cm061001Mapper.findStockIdByProductCode(productId);
                if (stockId == null) {
                    throw new IllegalStateException(CM061001MessageConstant.STOCK_NOT_FOUND);
                }

                cm061001Mapper.insertStockDetail(stockId, -1L * quantity, null, actor, actor);
            }

            cm061001Mapper.updateOrderStatusOnApprove(orderId, 2, "LOCAL_CARD_MOCK", actor);
            cm061001Mapper.updateOrderNetProfit(orderId, actor);
            sendOrderConfirmationEmail(summary, orderItems, "CARD");
        }

        return cm061001Mapper.findOrderSummary(orderId);
    }

    /**
     * 注文完了（購入確定）＋本人確認
     * @param orderId 注文ID
     * @param memberNumber 会員番号
     */
    @Override
    @Transactional
    public void completeOrder(Long orderId, String memberNumber) {
        validateOrderAccessInput(orderId, memberNumber);

        final String owner = cm061001Mapper.findOrderOwner(orderId);
        if (owner == null || !memberNumber.equals(owner)) {
            log.warn(CM061001MessageConstant.LOG_ORDER_SUMMARY_ACCESS_DENIED, orderId, memberNumber);
            throw new AccessDeniedException("該当の注文にアクセスできません。");
        }
        final String confirmDate = dateCalculator.DateString();
        final String refundDeadline =
                dateCalculator.convertToYYYYMMDD(dateCalculator.tokyoTime().plusDays(90));

        cm061001Mapper.updateOrderStatusToCompleted(orderId, refundDeadline);

        // 銀行振込の購入確定時にも注文確認メールを送信する
        final OrderSummary summary = cm061001Mapper.findOrderSummary(orderId);
        final List<OrderItemInfo> orderItems = cm061001Mapper.findOrderItems(orderId);
        sendOrderConfirmationEmail(summary, orderItems, "BANK");

        log.info(
                "[注文完了] orderId={}, member={}, confirmDate={}, refundDeadline={}",
                orderId,
                memberNumber,
                confirmDate,
                refundDeadline);
    }

    /**
     * 注文作成リクエスト検証
     * @param request 注文作成リクエストオブジェクト
     */
    private void validateCreateOrderRequest(OrderCreate request) {
        require(request, CM061001MessageConstant.ORDER_REQUEST_REQUIRED);
        require(request.getUserId(), CM061001MessageConstant.USER_INFO_REQUIRED);
        requireText(request.getMemberNumber(), CM061001MessageConstant.MEMBER_NUMBER_REQUIRED);
        if (request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            throw new IllegalArgumentException(CM061001MessageConstant.CART_ITEMS_REQUIRED);
        }
        if (request.getTotalAmount() != null && request.getTotalAmount() < 0) {
            throw new IllegalArgumentException(CM061001MessageConstant.TOTAL_AMOUNT_INVALID);
        }
        if (request.getShippingFee() != null && request.getShippingFee() < 0) {
            throw new IllegalArgumentException(CM061001MessageConstant.SHIPPING_FEE_INVALID);
        }
        requireText(request.getPaymentMethod(), CM061001MessageConstant.PAYMENT_METHOD_REQUIRED);
    }

    /**
     * 必須値の検証
     * 
     * @param value 検証対象値
     * @param message 例外メッセージ
     */
    private void require(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     * 必須テキスト値の検証
     * 
     * @param value 検証対象文字列
     * @param message 例外メッセージ
     */
    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 注文アクセス時の共通入力値検証
     * @param orderId 注文ID
     * @param memberNumber 会員番号
     */
    private void validateOrderAccessInput(Long orderId, String memberNumber) {
        if (orderId == null) {
            throw new IllegalArgumentException(CM061001MessageConstant.ORDER_ID_REQUIRED);
        }
        if (memberNumber == null || memberNumber.isBlank()) {
            throw new IllegalArgumentException(CM061001MessageConstant.MEMBER_NUMBER_REQUIRED);
        }
    }

    /**
     * フロントから渡される支払方法文字列
     * 
     * @param rawPaymentMethod "1": 銀行振込, "2": クレジットカード
     * @return コード値（1 または 2）
     */
    private int resolvePaymentValue(String rawPaymentMethod) {
        final int method = parseIntSafe(rawPaymentMethod);

        if (method == 1) {
            return 1;
        }
        if (method == 2) {
            return 2;
        }

        throw new IllegalArgumentException(
                CM061001MessageConstant.PAYMENT_METHOD_UNSUPPORTED.replace("{}", rawPaymentMethod));
    }

    /**
     * 文字列を int に安全変換。変換失敗時は -1 を返却
     * 
     * @param value 文字列値
     * @return 変換後の整数、失敗時は -1
     */
    private int parseIntSafe(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("결제 방법 변환 실패: {}", value);
            return -1;
        }
    }

    /**
     * 銀行振込案内メール送信
     * 
     * @param orderId 注文ID
     * @param orderNumber 注文番号
     * @param memberNumber 会員番号
     * @param recipientName 受取人名
     */
    private void sendBankTransferGuideEmail(Long orderId, String orderNumber, String memberNumber, String recipientName) {
        sendBankTransferGuideEmailWithResponse(orderId, orderNumber, memberNumber, recipientName);
    }

    private void sendOrderConfirmationEmail(OrderSummary summary, List<OrderItemInfo> orderItems, String paymentMethod) {
        try {
            if (summary == null || summary.getBuyerEmail() == null || summary.getBuyerEmail().isBlank()) {
                log.warn("注文確認メール送信スキップ: メールアドレスなし");
                return;
            }

            final EmailDto emailInfo = new EmailDto();
            emailInfo.setSendEmail(summary.getBuyerEmail());
            emailInfo.setName(summary.getBuyerName());
            emailInfo.setOrderDate(dateCalculator.convertToYYMMDD(dateCalculator.tokyoTime(), 0));
            emailInfo.setOrderNumber(summary.getOrderNumber());
            emailInfo.setPaymentMethod(resolvePaymentMethodLabel(paymentMethod));
            emailInfo.setPaymentAmount(formatAmount(summary.getTotalAmount()));
            emailInfo.setOrderItems(buildOrderItemsText(orderItems));

            emailService.sendOrderConfirmation(emailInfo);
        } catch (Exception ex) {
            log.error("注文確認メール送信失敗: orderId={}", summary != null ? summary.getOrderId() : null, ex);
        }
    }

    private String buildOrderItemsText(List<OrderItemInfo> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < orderItems.size(); i++) {
            final OrderItemInfo item = orderItems.get(i);
            final String productName = item.getProductName() != null ? item.getProductName() : item.getProductId();
            sb.append("・").append(productName).append(" X ").append(item.getQuantity());
            if (i < orderItems.size() - 1) {
                sb.append("<br>\n");
            }
        }
        return sb.toString();
    }

    private String resolvePaymentMethodLabel(String paymentMethod) {
        if ("BANK".equalsIgnoreCase(paymentMethod)) {
            return "銀行振込";
        }
        if ("CARD".equalsIgnoreCase(paymentMethod)) {
            return "クレジットカード";
        }
        return paymentMethod != null ? paymentMethod : "";
    }

    private String formatAmount(Number amount) {
        if (amount == null) {
            return "";
        }
        return new DecimalFormat("#,###").format(amount.longValue());
    }

    private String resolveAuditUser(Long orderId, String memberNumber) {
        final String userId = cm061001Mapper.findOrderUserId(orderId);
        if (userId != null && !userId.isBlank()) {
            return userId;
        }
        return memberNumber;
    }

    private String generateMockTid() {
        final String prefix = dateCalculator.tokyoTime().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        final StringBuilder suffix = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            int idx = (int) (Math.random() * chars.length());
            suffix.append(chars.charAt(idx));
        }
        return prefix + "GU00" + suffix;
    }

    private ResponseEntity<ResponseModel<Integer>> sendBankTransferGuideEmailWithResponse(
            Long orderId,
            String orderNumber,
            String memberNumber,
            String recipientName) {
        log.info(CM061001MessageConstant.EMAIL_START);
        try {
            final String customerEmail = cm061001Mapper.findEmailByMemberNumber(memberNumber);
            if (customerEmail == null || customerEmail.isBlank()) {
                log.error(CM061001MessageConstant.EMAIL_CUSTOMER_INFO_NOT_FOUND);
                return ResponseEntity.ok(createResponseModel(null, false,
                        CM061001MessageConstant.EMAIL_CUSTOMER_INFO_NOT_FOUND));
            }

            final EmailDto emailInfo = new EmailDto();
            emailInfo.setSendEmail(customerEmail);
            emailInfo.setName(recipientName);
            emailInfo.setOrderNumber(orderNumber);
            emailInfo.setOrderDate(dateCalculator.DateString());
            emailInfo.setDueDate(dateCalculator.convertToYYYYMMDD(dateCalculator.tokyoTime().plusDays(3)));

            emailService.sendBankTransferGuide(emailInfo);
            log.info(CM061001MessageConstant.EMAIL_SEND_SUCCESS, orderNumber);
        } catch (IllegalArgumentException iae) {
            log.error(CM061001MessageConstant.EMAIL_SEND_INPUT_ERROR, orderNumber, iae.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(0, false,
                            CM061001MessageConstant.EMAIL_SEND_FAILED_INPUT.replace("{}", iae.getMessage())));
        } catch (RuntimeException re) {
            log.error(CM061001MessageConstant.EMAIL_SEND_SYSTEM_ERROR, orderNumber, re.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CM061001MessageConstant.EMAIL_SEND_FAILED_SYSTEM));
        } catch (Exception ex) {
            log.error(CM061001MessageConstant.EMAIL_SEND_GENERAL_ERROR, orderNumber, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CM061001MessageConstant.EMAIL_SEND_FAILED_GENERAL));
        }
        return null;
    }

    /**
     * 共通レスポンスモデル生成
     * 
     * @param resultList 結果データ
     * @param result 処理結果
     * @param message 応答メッセージ
     * @return ResponseModel オブジェクト
     */
    private <T> ResponseModel<T> createResponseModel(T resultList, boolean result, String message) {
        return ResponseModel.<T>builder()
                .resultList(resultList)
                .result(result)
                .message(Objects.requireNonNull(message, CMMessageConstant.MESSAGE_NULL_NOT_ALLOWED))
                .build();
    }
}
