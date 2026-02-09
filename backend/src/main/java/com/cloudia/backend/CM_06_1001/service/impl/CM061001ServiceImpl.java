package com.cloudia.backend.CM_06_1001.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
     * 주문 생성
     * @param request 주문 생성 요청 객체
     * @return 생성된 주문 요약 정보
     */
    @Override
    @Transactional
    public OrderSummary createOrder(OrderCreate request) {
        log.info("주문 생성: request={}", request);

        validateCreateOrderRequest(request);

        final ShippingInfo shippingInfo = request.getShipping();
        final List<Long> cartItemIds = request.getCartItemIds();
        final List<CartItemResponse> cartItems =
                cm061001Mapper.selectCartItemsForOrder(request.getUserId(), cartItemIds);
        if (cartItems == null || cartItems.isEmpty() || cartItems.size() != cartItemIds.size()) {
            throw new IllegalArgumentException(CM061001MessageConstant.CART_ITEMS_NOT_FOUND);
        }

        // 주문번호는 회원별로 결제번호는 전역 7자리 채번
        final String orderNumber = cm061001Mapper.selectNextOrderNumberByMember(request.getMemberNumber());
        final String paymentOrderNumber = cm061001Mapper.selectNextOrderNumber();
        final int shippingCost = request.getShippingFee() != null ? request.getShippingFee() : 0;
        // 총 금액은 장바구니 스냅샷 기반으로 계산 (클라이언트 값 신뢰하지 않음)
        final int subtotal =
                cartItems.stream()
                        .mapToInt(item -> item.getLineTotal() != null ? item.getLineTotal() : 0)
                        .sum();
        final int totalAmount = subtotal + shippingCost;

        // 수령인/연락처: 배송정보가 없으면 최소한 memberNumber 를 이름으로 세팅
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

        // 주문 정보 생성
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

        // BANK(계좌이체) 결제 처리
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

            // 계좌이체 메일 발송 (메일 실패가 주문 생성/재고 처리 흐름을 막지 않도록 로그만 남김)
            sendBankTransferGuideEmail(orderId, orderNumber, request.getMemberNumber(), recipientName);
        }

        log.info(CM061001MessageConstant.LOG_ORDER_CREATED, orderId, orderNumber);

        return cm061001Mapper.findOrderSummary(orderId);
    }

    /**
     * 주문 요약 조회 + 본인 여부 검증
     * @param orderId 주문 ID
     * @param memberNumber 회원 번호
     * @return 주문 요약 정보
     */
    @Override
    @Transactional(readOnly = true)
    public OrderSummary getOrderSummary(Long orderId, String memberNumber) {
        validateOrderAccessInput(orderId, memberNumber);

        final String owner = cm061001Mapper.findOrderOwner(orderId);
        if (owner == null || !owner.equals(memberNumber)) {
            log.warn(CM061001MessageConstant.LOG_ORDER_SUMMARY_ACCESS_DENIED, orderId, memberNumber);
            throw new AccessDeniedException("해당 주문에 접근할 수 없습니다.");
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
     * 최신 결제 상태 조회
     * @param orderNumber 주문 번호
     * @return 최신 결제 정보 객체
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

    /**
     * 주문 완료(구매확정) + 본인 검증
     * @param orderId 주문 ID
     * @param memberNumber 회원 번호
     */
    @Override
    @Transactional
    public void completeOrder(Long orderId, String memberNumber) {
        validateOrderAccessInput(orderId, memberNumber);

        final String owner = cm061001Mapper.findOrderOwner(orderId);
        if (owner == null || !memberNumber.equals(owner)) {
            log.warn(CM061001MessageConstant.LOG_ORDER_SUMMARY_ACCESS_DENIED, orderId, memberNumber);
            throw new AccessDeniedException("해당 주문에 접근할 수 없습니다.");
        }
        final String confirmDate = dateCalculator.DateString();
        final String refundDeadline =
                dateCalculator.convertToYYYYMMDD(dateCalculator.tokyoTime().plusDays(90));

        cm061001Mapper.updateOrderStatusToCompleted(orderId, refundDeadline);

        log.info(
                "[주문 완료] orderId={}, member={}, confirmDate={}, refundDeadline={}",
                orderId,
                memberNumber,
                confirmDate,
                refundDeadline);
    }

    /**
     * 주문 생성 요청 검증
     * @param request 주문 생성 요청 객체
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
     * 필수 값 검증
     * 
     * @param value
     * @param message
     */
    private void require(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     * 필수 텍스트 값 검증
     * 
     * @param value
     * @param message
     */
    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 주문 접근시 공통 입력값 검증
     * @param orderId 주문 ID
     * @param memberNumber 회원 번호
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
     * 프론트에서 넘어온 결제방법 문자열
     * 
     * @param rawPaymentMethod "1": 계좌이체, "2": 신용카드
     * @return 코드값(1 또는 2)
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
     * 문자열을 int 로 안전하게 변환. 변환 실패 시 -1 반환
     * 
     * @param value 문자열 값
     * @return 변환된 정수, 실패 시 -1
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
     * 계좌이체 안내 이메일 발송
     * 
     * @param orderId 주문 ID
     * @param orderNumber 주문 번호
     * @param memberNumber 회원 번호
     * @param recipientName 수령인 이름
     */
    private void sendBankTransferGuideEmail(Long orderId, String orderNumber, String memberNumber, String recipientName) {
        sendBankTransferGuideEmailWithResponse(orderId, orderNumber, memberNumber, recipientName);
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
     * 공통 응답 모델 생성
     * 
     * @param resultList 결과 데이터
     * @param result     처리 결과
     * @param message    응답 메시지
     * @return ResponseModel 객체
     */
    private <T> ResponseModel<T> createResponseModel(T resultList, boolean result, String message) {
        return ResponseModel.<T>builder()
                .resultList(resultList)
                .result(result)
                .message(Objects.requireNonNull(message, CMMessageConstant.MESSAGE_NULL_NOT_ALLOWED))
                .build();
    }
}
