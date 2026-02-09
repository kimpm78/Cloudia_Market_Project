package com.cloudia.backend.common.service.impl;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.model.pg.PGReadyRequest;
import com.cloudia.backend.common.model.pg.PGApproveRequest;
import com.cloudia.backend.common.model.pg.PGCancelRequest;
import com.cloudia.backend.common.model.pg.PGDecryptRequest;
import com.cloudia.backend.common.model.pg.PGFailRequest;
import com.cloudia.backend.common.model.pg.PGResult;
import com.cloudia.backend.common.service.PaymentService;
import com.cloudia.backend.common.service.pg.PGProvider;
import com.cloudia.backend.common.service.pg.PGProviderRegistry;
import com.cloudia.backend.CM_06_1001.mapper.CM061001Mapper;
import com.cloudia.backend.CM_06_1001.model.OrderItemInfo;
import com.cloudia.backend.CM_06_1001.model.OrderSummary;
import com.cloudia.backend.CM_06_1001.model.PaymentInfo;
import org.springframework.beans.factory.annotation.Value;
import com.cloudia.backend.common.model.EmailDto;
import com.cloudia.backend.common.service.EmailService;
import com.cloudia.backend.common.util.DateCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final EmailService emailService;
    private final PGProviderRegistry providerRegistry;
    private final CM061001Mapper mapper;
    private final DateCalculator dateCalculator;

    @Value("${app.homepage.url}")
    private String homepageUrl;

    @Value("${app.api.url}")
    private String apiBaseUrl;
    
    /**
     * PG ready 호출
     * 
     * @param request PG 준비 요청 객체
     * @return PG 준비 결과와 주문 정보
     */
    @Override
    public ResponseModel<Map<String, Object>> ready(PGReadyRequest request) {
        try {
            validateReadyRequest(request);

            final Long orderId = request.getOrderId();
            final String pgType = request.getPgType();

            // PG ORDERNO는 서버에서 전역 유니크로 생성
            final String pgOrderNo = mapper.selectNextOrderNumber();
            request.setOrderNo(pgOrderNo);

            String paymentMethod = request.getPayMethod();
            if (paymentMethod == null || paymentMethod.isBlank())
                paymentMethod = "CARD";

            String apiBase = normalizeBaseUrl(apiBaseUrl);
            String frontBase = normalizeBaseUrl(homepageUrl);
            if (frontBase != null) {
                request.setHomeUrl(frontBase);
            }

            request.setReturnUrl(buildCallbackUrl(apiBase, "/pay/callback"));
            request.setCancelUrl(buildCallbackUrl(apiBase, "/pay/cancel-callback"));
            request.setFailUrl(buildCallbackUrl(apiBase, "/pay/fail-callback"));

            log.info("[결제 준비 요청] {}", request);

            String paymentId = UUID.randomUUID().toString();
            String paymentUserId = resolvePaymentUserId(orderId);

            PaymentInfo payment = PaymentInfo.builder()
                    .paymentId(paymentId)
                    .orderId(orderId)
                    .orderNumber(pgOrderNo)
                    .pgType(pgType)
                    .pgProvider(pgType)
                    .paymentMethod(paymentMethod)
                    .paymentStatusType("013")
                    .paymentStatusCode("1")
                    .amount(request.getAmount())
                    .transactionId(null)
                    .createdBy(paymentUserId)
                    .createdAt(dateCalculator.tokyoTime())
                    .updatedBy(paymentUserId)
                    .updatedAt(dateCalculator.tokyoTime())
                    .build();

            mapper.insertPayment(payment);

            PGProvider provider = providerRegistry.getProvider(pgType);

            PGResult result = provider.ready(request);

            boolean hasRedirect = result != null &&
                    ((result.getRedirectUrl() != null && !result.getRedirectUrl().isBlank())
                            || (result.getUrl() != null && !result.getUrl().isBlank()));
            boolean hasHtml = result != null && result.getHtml() != null && !result.getHtml().isBlank();
            boolean readySuccess = result != null && (result.isSuccess() || hasRedirect || hasHtml);

            String resolvedTid = (result != null && result.getTid() != null && !result.getTid().isBlank())
                    ? result.getTid()
                    : null;
            payment.setTransactionId(resolvedTid);
            payment.setResultCode(result != null ? result.getResultCode() : "NULL_RESULT");
            payment.setResultMsg(result != null ? result.getResultMsg() : "PG ready result is null");
            payment.setPaymentStatusCode(readySuccess ? "1" : "3");
            payment.setUpdatedAt(dateCalculator.tokyoTime());

            mapper.updatePayment(payment);

            Map<String, Object> data = new HashMap<>();
            data.put("pgResult", result);
            data.put("orderId", orderId);
            data.put("pgOrderNo", pgOrderNo);
            data.put("redirectUrl",
                    result != null ? (result.getRedirectUrl() != null ? result.getRedirectUrl() : result.getUrl())
                            : null);
            data.put("html", result != null ? result.getHtml() : null);

            return ResponseModel.<Map<String, Object>>builder()
                    .result(readySuccess)
                    .message(readySuccess ? "PG ready success" : "PG ready fail")
                    .resultList(data)
                    .build();

        } catch (Exception e) {
            log.error("[PG READY] ERROR", e);
            return ResponseModel.<Map<String, Object>>builder()
                    .result(false)
                    .message("PG 준비 에러: " + e.getMessage())
                    .resultList(null)
                    .build();
        }
    }

    /**
     * 승인 요청 + DB 업데이트
     * 
     * @param request PG 승인 요청 객체
     * @return PG 승인 결과와 관련 정보
     */
    @Override
    @Transactional
    public ResponseModel<Map<String, Object>> approve(PGApproveRequest request) {
        try {
            validateApproveRequest(request);

            String pgType = resolvePgType(request.getPgType());
            request.setPgType(pgType);

            // tid로 결제 1건 확정 (없으면 orderId/orderNumber로 보완)
            final String tid = request.getTid();
            PaymentInfo payment = resolvePayment(tid, request.getOrderId(), request.getOrderNumber(), true);
            if (payment == null) {
                throw new IllegalArgumentException("payment not found by tid/orderId/orderNumber.");
            }
            log.info(
                    "[PG APPROVE] paymentId={}, orderId={}, orderNumber={}, tid={}",
                    payment.getPaymentId(),
                    payment.getOrderId(),
                    payment.getOrderNumber(),
                    tid);
            final Long orderId = payment.getOrderId();
            final String pgOrderNo = payment.getOrderNumber();
            final Integer expectedAmount = payment.getAmount();
            if (request.getAmount() == null && expectedAmount != null) {
                request.setAmount(expectedAmount);
            }

            String statusCode = String.valueOf(payment.getPaymentStatusCode());
            if ("2".equals(statusCode)) {
                return ResponseModel.<Map<String, Object>>builder()
                        .result(true)
                        .message("Payment already approved.")
                        .resultList(Map.of("success", true, "idempotent", true))
                        .build();
            }
            if ("3".equals(statusCode)) {
                return ResponseModel.<Map<String, Object>>builder()
                        .result(false)
                        .message("Payment already failed.")
                        .resultList(Map.of("success", false, "idempotent", true))
                        .build();
            }

            // PG 승인 호출 (tid/amount만 사용)
            PGProvider provider = providerRegistry.getProvider(pgType);
            PGResult result = provider.approve(request);

            boolean success = result != null && result.isSuccess();

            // ORDERNO(있으면) + amount
            if (success && result.getOrderNo() != null && !result.getOrderNo().isBlank()) {
                if (pgOrderNo != null && !pgOrderNo.isBlank() && !pgOrderNo.equals(result.getOrderNo())) {
                    success = false;
                    result.setResultCode("ORDER_MISMATCH");
                    result.setResultMsg("Order number mismatch.");
                }
            }

            if (success && expectedAmount != null) {
                // PG 응답 amount가 문자열일 수 있으니 숫자만 비교
                String pgAmtRaw = result != null ? result.getAmount() : null;
                if (pgAmtRaw != null && !pgAmtRaw.isBlank()) {
                    String digits = pgAmtRaw.replaceAll("[^0-9]", "");
                    if (!digits.isBlank() && Integer.parseInt(digits) != expectedAmount) {
                        success = false;
                        result.setResultCode("AMOUNT_MISMATCH");
                        result.setResultMsg("Amount mismatch.");
                    }
                } else if (request.getAmount() != null && !request.getAmount().equals(expectedAmount)) {
                    success = false;
                    result.setResultCode("AMOUNT_MISMATCH");
                    result.setResultMsg("Amount mismatch.");
                }
            }

            // (성공 시) 재고 처리/카트 비활성화는 orderId로
            String paymentUserId = resolvePaymentUserId(orderId);

            List<OrderItemInfo> orderItems = null;
            if (success) {
                mapper.deactivateCartItemsByOrderId(orderId, paymentUserId);

                orderItems = mapper.findOrderItems(orderId);
                if (orderItems == null || orderItems.isEmpty()) {
                    throw new IllegalStateException("주문 상품 정보를 찾을 수 없습니다.");
                }

                for (OrderItemInfo item : orderItems) {
                    String productId = item.getProductId();
                    int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                    if (productId == null || productId.isBlank() || quantity < 1) {
                        throw new IllegalStateException("재고 차감 대상 정보가 올바르지 않습니다.");
                    }

                    int affected = mapper.decreaseStock(productId, quantity);
                    if (affected != 1) {
                        throw new IllegalStateException("재고가 부족하여 주문을 완료할 수 없습니다.");
                    }

                    Long stockId = mapper.findStockIdByProductCode(productId);
                    if (stockId == null) {
                        throw new IllegalStateException("재고 정보를 찾을 수 없습니다.");
                    }

                    mapper.insertStockDetail(stockId, -1L * quantity, null, paymentUserId, paymentUserId);
                }
            }

            // DB 업데이트는 tid 기준
            mapper.updatePaymentStatusOnApprove(
                    payment.getPaymentId(),
                    orderId,
                    request.getTid(),
                    result != null ? result.getResultCode() : "NULL_RESULT",
                    result != null ? result.getResultMsg() : "PG 승인 결과가 없습니다.",
                    success ? "2" : "3",
                    success ? dateCalculator.tokyoTime().toString() : null,
                    paymentUserId);

            mapper.updateOrderStatusOnApprove(
                    orderId,
                    success ? 2 : 8,
                    result != null ? result.getResultMsg() : "PG approve result is null",
                    paymentUserId);

            if (success) {
                mapper.updateOrderNetProfit(orderId, paymentUserId);
            }

            if (success) {
                sendOrderConfirmationEmail(orderId, pgOrderNo, expectedAmount, payment.getPaymentMethod(), orderItems);
            }

            return ResponseModel.<Map<String, Object>>builder()
                    .result(success)
                    .message(result != null ? result.getResultMsg() : (success ? "OK" : "FAIL"))
                    .resultList(Map.of("success", success, "pgResult", result))
                    .build();

        } catch (Exception e) {
            log.error("[PG APPROVE ERROR]", e);
            return ResponseModel.<Map<String, Object>>builder()
                    .result(false)
                    .message("PG 승인 중 오류 발생: " + e.getMessage())
                    .resultList(null)
                    .build();
        }
    }

    private void sendOrderConfirmationEmail(Long orderId, String orderNumber, Integer amount, String paymentMethod,
            List<OrderItemInfo> orderItems) {
        log.info("[ORDER CONFIRM EMAIL] start. orderId={}, orderNumber={}", orderId, orderNumber);
        try {
            OrderSummary summary = mapper.findOrderSummary(orderId);
            if (summary == null || summary.getBuyerEmail() == null || summary.getBuyerEmail().isBlank()) {
                log.warn("[ORDER CONFIRM EMAIL] customerEmail is empty. orderId={}", orderId);
                return;
            }

            EmailDto emailInfo = new EmailDto();
            emailInfo.setSendEmail(summary.getBuyerEmail());
            emailInfo.setName(summary.getBuyerName());
            emailInfo.setOrderDate(dateCalculator.convertToYYMMDD(dateCalculator.tokyoTime(), 0));
            emailInfo.setOrderNumber(orderNumber != null ? orderNumber : summary.getOrderNumber());
            emailInfo.setPaymentMethod(resolvePaymentMethodLabel(paymentMethod));
            emailInfo.setPaymentAmount(formatAmount(amount != null ? amount : summary.getTotalAmount()));
            emailInfo.setOrderItems(buildOrderItemsText(orderItems));

            emailService.sendOrderConfirmation(emailInfo);
            log.info("[ORDER CONFIRM EMAIL] success. orderId={}, orderNumber={}", orderId, orderNumber);
        } catch (IllegalArgumentException iae) {
            log.error("[ORDER CONFIRM EMAIL] input error. orderId={}, message={}", orderId, iae.getMessage());
        } catch (RuntimeException re) {
            log.error("[ORDER CONFIRM EMAIL] system error. orderId={}, message={}", orderId, re.getMessage(), re);
        } catch (Exception ex) {
            log.error("[ORDER CONFIRM EMAIL] general error. orderId={}, message={}", orderId, ex.getMessage(), ex);
        }
    }

    private String buildOrderItemsText(List<OrderItemInfo> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return "";
        }

        StringBuilder str = new StringBuilder();
        for (int i = 0; i < orderItems.size(); i++) {
            OrderItemInfo data = orderItems.get(i);
            String productName = data.getProductName() != null ? data.getProductName() : data.getProductId();
            str.append("・")
                    .append(productName)
                    .append(" X ")
                    .append(data.getQuantity());
            if (i < orderItems.size() - 1) {
                str.append("<br>\n");
            }
        }
        return str.toString();
    }

    private String resolvePaymentMethodLabel(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return "";
        }
        if ("BANK".equalsIgnoreCase(paymentMethod)) {
            return "계좌이체";
        }
        if ("CARD".equalsIgnoreCase(paymentMethod)) {
            return "신용카드";
        }
        return paymentMethod;
    }

    private String formatAmount(Integer amount) {
        if (amount == null) {
            return "";
        }
        return new DecimalFormat("#,###").format(amount);
    }

    /**
     * CANCEL — PG 취소 + DB 업데이트
     * 
     * @param request PG 취소 요청 객체
     * @return PG 취소 결과와 관련 정보
     */
    @Override
    @Transactional
    public ResponseModel<Map<String, Object>> cancel(PGCancelRequest request) {
        try {
            log.info("[결제 취소] 시작: orderId={}, tid={}", request.getOrderId(), request.getTid());

            PGProvider provider = providerRegistry.getProvider(request.getPgType());
            PGResult result = provider.cancel(request);

            boolean success = result.isSuccess();

            String paymentUserId = resolvePaymentUserId(request.getOrderId());

            mapper.updatePaymentStatusOnCancel(
                    request.getPaymentId(),
                    result.getResultCode(),
                    result.getResultMsg(),
                    paymentUserId);

            mapper.updateOrderStatusOnCancel(
                    request.getOrderId(),
                    8
            );

            Map<String, Object> data = new HashMap<>();
            data.put("success", success);
            data.put("pgResult", result);

            return ResponseModel.<Map<String, Object>>builder()
                    .result(success)
                    .message(result.getResultMsg())
                    .resultList(data)
                    .build();

        } catch (Exception e) {
            log.error("[PG CANCEL ERROR]", e);

            return ResponseModel.<Map<String, Object>>builder()
                    .result(false)
                    .message("PG 취소 중 오류 발생: " + e.getMessage())
                    .resultList(null)
                    .build();
        }
    }

    /**
     * FAIL — 내부 실패 처리(사용자 닫힘/PG 실패 리다이렉트 등)
     * 
     * @param request 실패 처리 요청 객체
     * @return 처리 결과
     */
    @Override
    @Transactional
    public ResponseModel<Map<String, Object>> fail(PGFailRequest request) {
        try {
            validateFailRequest(request);
            final String tid = request.getTid();
            final String orderNumber = request.getOrderNumber();
            final Long orderId = request.getOrderId();

            log.info(
                    "[PG FAIL REQUEST] orderId={}, orderNumber={}, tid={}",
                    orderId,
                    orderNumber,
                    tid);

            PaymentInfo payment = resolvePayment(tid, orderId, orderNumber, true);
            if (payment == null) {
                throw new IllegalArgumentException("결제 정보를 찾을 수 없습니다.");
            }

            log.info(
                    "[PG FAIL LOOKUP] paymentId={}, orderId={}, orderNumber={}, tid={}",
                    payment.getPaymentId(),
                    payment.getOrderId(),
                    payment.getOrderNumber(),
                    payment.getTransactionId());

            String status = String.valueOf(payment.getPaymentStatusCode());

            // 013: 2=승인, 3=실패 (코드일람 기준)
            if ("2".equals(status)) {
                return ResponseModel.<Map<String, Object>>builder()
                        .result(true)
                        .message("이미 승인 완료된 결제입니다.")
                        .resultList(Map.of("success", true, "idempotent", true))
                        .build();
            }
            if ("3".equals(status)) {
                return ResponseModel.<Map<String, Object>>builder()
                        .result(true)
                        .message("이미 실패 처리된 결제입니다.")
                        .resultList(Map.of("success", true, "idempotent", true))
                        .build();
            }

            final String reason = (request.getReason() != null && !request.getReason().isBlank())
                    ? request.getReason()
                    : "사용자 또는 PG 처리 실패";

            // tid 기준 업데이트
            mapper.updatePaymentStatusToFailed(
                    payment.getPaymentId(),
                    tid,
                    "FAIL",
                    reason,
                    resolvePaymentUserId(payment.getOrderId()),
                    dateCalculator.tokyoTime());

            // 주문 상태 업데이트는 payment.orderId 사용
            mapper.updateOrderStatusOnApprove(
                    payment.getOrderId(),
                    8,
                    reason,
                    resolvePaymentUserId(payment.getOrderId()));

            return ResponseModel.<Map<String, Object>>builder()
                    .result(true)
                    .message("결제를 실패 처리했습니다.")
                    .resultList(Map.of("success", true))
                    .build();

        } catch (Exception e) {
            log.error("[PG FAIL ERROR]", e);
            return ResponseModel.<Map<String, Object>>builder()
                    .result(false)
                    .message("PG 실패 처리 중 오류가 발생했습니다: " + e.getMessage())
                    .resultList(null)
                    .build();
        }
    }

    /**
     * DECRYPT — PG encData 복호화
     *
     * @param request 복호화 요청 객체
     * @return 복호화 결과와 관련 정보
     */
    @Override
    public ResponseModel<Map<String, Object>> decrypt(PGDecryptRequest request) {
        try {
            if (request == null || request.getEncData() == null || request.getEncData().isBlank()) {
                throw new IllegalArgumentException("encData is required.");
            }

            String pgType = (request.getPgType() == null || request.getPgType().isBlank())
                    ? "COOKIEPAY"
                    : request.getPgType();
            request.setPgType(pgType);

            PGProvider provider = providerRegistry.getProvider(pgType);
            PGResult decrypted = provider.decrypt(request);

            boolean success = decrypted != null && decrypted.isSuccess();
            Map<String, Object> data = new HashMap<>();
            data.put("success", success);
            data.put("pgResult", decrypted);

            return ResponseModel.<Map<String, Object>>builder()
                    .result(success)
                    .message(success ? "PG decrypt success" : "PG decrypt failure")
                    .resultList(data)
                    .build();
        } catch (Exception e) {
            log.error("[PG DECRYPT ERROR]", e);
            return ResponseModel.<Map<String, Object>>builder()
                    .result(false)
                    .message("PG decrypt 중 오류 발생: " + e.getMessage())
                    .resultList(null)
                    .build();
        }
    }

    /**
     * CALLBACK — PG returnURL 복호화 처리
     * 
     * @param params PG 콜백 파라미터
     * @return 복호화 결과와 관련 정보
     */
    @Override
    public ResponseModel<Map<String, Object>> callback(Map<String, String> params) {

        try {
            Map<String, String> normalized = normalizeParams(params);
            log.info("[PG CALLBACK] keys={}", normalized.keySet());

            String pgType = firstNonBlank(normalized, "pgType", "pgtype", "PGTYPE", "pg_type", "PG_TYPE");
            if (pgType == null || pgType.isBlank()) {
                pgType = "COOKIEPAY";
            }

            PGProvider provider = providerRegistry.getProvider(pgType);

            String encData = firstNonBlank(normalized, "encData", "encdata", "enc_data", "ENCDATA", "ENC_DATA");
            PGResult decrypted = null;
            if (encData != null && !encData.isBlank()) {
                PGDecryptRequest req = new PGDecryptRequest();
                req.setEncData(encData);
                decrypted = provider.decrypt(req);
            }

            String tid = firstNonBlank(normalized, "tid", "TID", "transactionId", "transaction_id", "TRANSACTIONID");
            String resultCode = firstNonBlank(normalized, "resultCode", "result_code", "rtn_cd", "code",
                    "RESULTCODE", "RESULT_CODE", "RTN_CD", "CODE");
            String resultMsg = firstNonBlank(normalized, "resultMsg", "result_msg", "rtn_msg", "message",
                    "resultMessage", "RESULTMSG", "RESULT_MSG", "RTN_MSG", "MESSAGE", "RESULTMESSAGE");
            String orderId = firstNonBlank(normalized, "orderId", "order_id", "ORDERID", "ORDER_ID");
            String orderNumber = firstNonBlank(normalized, "orderNumber", "order_number", "ORDERNUMBER", "ORDER_NUMBER");
            String orderNo = firstNonBlank(normalized, "orderNo", "ORDERNO", "orderno");
            if ((orderNumber == null || orderNumber.isBlank()) && orderNo != null && !orderNo.isBlank()) {
                orderNumber = orderNo;
            }
            if ((orderNumber == null || orderNumber.isBlank()) && orderId != null && !orderId.isBlank()) {
                String trimmedOrderId = orderId.trim();
                boolean looksLikeOrderNumber =
                        trimmedOrderId.startsWith("0") || !trimmedOrderId.matches("\\d+");
                if (looksLikeOrderNumber) {
                    orderNumber = trimmedOrderId;
                    orderId = null;
                }
            }
            log.info("[PG CALLBACK] tid={}, orderId={}, orderNumber={}", tid, orderId, orderNumber);

            if (decrypted != null) {
                if (tid == null || tid.isBlank()) {
                    tid = decrypted.getTid();
                }
                if (resultCode == null || resultCode.isBlank()) {
                    resultCode = decrypted.getResultCode();
                }
                if (resultMsg == null || resultMsg.isBlank()) {
                    resultMsg = decrypted.getResultMsg();
                }
            }

            if (tid != null && !tid.isBlank()) {
                updateTransactionIdIfMissing(orderId, orderNumber, tid);
            }

            String statusParam = firstNonBlank(normalized, "status", "resultStatus", "result_status",
                    "STATUS", "RESULTSTATUS", "RESULT_STATUS");
            String status = normalizeStatus(statusParam);
            if (status == null) {
                if (tid == null || tid.isBlank()) {
                    status = "cancel";
                } else if (resultCode == null || resultCode.isBlank() || "0000".equals(resultCode)) {
                    status = "success";
                } else {
                    status = "fail";
                }
            }

            String frontBase = normalizeBaseUrl(homepageUrl);
            String apiBase = normalizeBaseUrl(apiBaseUrl);
            String redirectUrl = buildFrontRedirectUrl(frontBase, orderId, orderNumber, status, resultCode, resultMsg,
                    tid, pgType, encData, apiBase);

            Map<String, Object> data = new HashMap<>();
            data.put("success", "success".equals(status));
            data.put("status", status);
            data.put("orderId", orderId);
            data.put("orderNumber", orderNumber);
            data.put("tid", tid);
            data.put("resultCode", resultCode);
            data.put("resultMsg", resultMsg);
            data.put("redirectUrl", redirectUrl);
            data.put("pgResult", decrypted);

            return ResponseModel.<Map<String, Object>>builder()
                    .result("success".equals(status))
                    .message("PG callback " + status)
                    .resultList(data)
                    .build();

        } catch (Exception e) {
            log.error("[PG CALLBACK ERROR]", e);

            return ResponseModel.<Map<String, Object>>builder()
                    .result(false)
                    .message("PG callback 중 오류 발생: " + e.getMessage())
                    .resultList(null)
                    .build();
        }
    }

    private String normalizeBaseUrl(String raw) {
        if (raw == null) {
            return null;
        }
        String base = raw.trim();
        if (base.isBlank()) {
            return null;
        }
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    private void validateReadyRequest(PGReadyRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is null");
        }
        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("orderId is required before PG ready() call.");
        }
        if (request.getPgType() == null || request.getPgType().isBlank()) {
            throw new IllegalArgumentException("pgType is required.");
        }
    }

    private void validateApproveRequest(PGApproveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is null");
        }
        if (request.getTid() == null || request.getTid().isBlank()) {
            throw new IllegalArgumentException("tid is required.");
        }
    }

    private void validateFailRequest(PGFailRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 값이 비어 있습니다.");
        }
    }

    private String resolvePgType(String pgType) {
        return (pgType == null || pgType.isBlank()) ? "COOKIEPAY" : pgType;
    }

    private PaymentInfo resolvePayment(
            String tid,
            Long orderId,
            String orderNumber,
            boolean updateTidIfMissing) {
        PaymentInfo payment = null;
        if (tid != null && !tid.isBlank()) {
            payment = mapper.findLatestPaymentByTransactionId(tid);
        }
        if (payment == null && orderNumber != null && !orderNumber.isBlank()) {
            payment = mapper.findLatestPayment(orderNumber);
        }
        if (payment == null && orderId != null) {
            payment = mapper.findLatestPaymentByOrderId(orderId);
        }
        if (updateTidIfMissing && payment != null && tid != null && !tid.isBlank()) {
            String existingTid = payment.getTransactionId();
            if (existingTid == null || existingTid.isBlank()) {
                mapper.updatePaymentTransactionId(payment.getPaymentId(), tid);
            }
        }
        return payment;
    }

    private String resolvePaymentUserId(Long orderId) {
        if (orderId == null) {
            return "system";
        }
        try {
            String userId = mapper.findOrderUserId(orderId);
            if (userId != null && !userId.isBlank()) {
                return userId;
            }
        } catch (Exception e) {
            log.warn("[PG READY] Failed to resolve payment actor for orderId={}", orderId, e);
        }
        return "system";
    }

    private String buildCallbackUrl(String apiBase, String path) {
        String target = apiBase != null ? apiBase : "";
        return new StringBuilder(target).append(path).toString();
    }

    private String buildFrontRedirectUrl(String baseUrl, String orderId, String orderNumber, String status,
            String resultCode, String resultMsg, String tid, String pgType, String encData, String apiBase) {
        String target = baseUrl != null ? baseUrl : "";
        StringBuilder sb = new StringBuilder(target).append("/payment-return.html");
        boolean hasQuery = false;
        hasQuery = appendParam(sb, hasQuery, "status", status);
        hasQuery = appendParam(sb, hasQuery, "orderId", orderId);
        hasQuery = appendParam(sb, hasQuery, "orderNumber", orderNumber);
        hasQuery = appendParam(sb, hasQuery, "orderNo", orderNumber);
        hasQuery = appendParam(sb, hasQuery, "tid", tid);
        hasQuery = appendParam(sb, hasQuery, "resultCode", resultCode);
        hasQuery = appendParam(sb, hasQuery, "resultMsg", resultMsg);
        hasQuery = appendParam(sb, hasQuery, "pgType", pgType);
        hasQuery = appendParam(sb, hasQuery, "apiBase", apiBase);
        appendParam(sb, hasQuery, "encData", encData);
        return sb.toString();
    }

    private boolean appendParam(StringBuilder sb, boolean hasQuery, String key, String value) {
        if (value == null || value.isBlank()) {
            return hasQuery;
        }
        sb.append(hasQuery ? '&' : '?');
        sb.append(key).append('=').append(urlEncode(value));
        return true;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String normalizeStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().toLowerCase();
        if ("cancel".equals(normalized) || "canceled".equals(normalized) || "cancelled".equals(normalized)) {
            return "cancel";
        }
        if ("fail".equals(normalized) || "failed".equals(normalized) || "error".equals(normalized)) {
            return "fail";
        }
        if ("success".equals(normalized) || "ok".equals(normalized) || "completed".equals(normalized)) {
            return "success";
        }
        return null;
    }

    private Map<String, String> normalizeParams(Map<String, String> input) {
        Map<String, String> out = new HashMap<>();
        if (input == null) {
            return out;
        }
        for (Map.Entry<String, String> entry : input.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            String key = entry.getKey().trim();
            if (key.isBlank()) {
                continue;
            }
            String value = entry.getValue() == null ? "" : entry.getValue().trim();
            out.put(key, value);
        }
        return out;
    }

    private String firstNonBlank(Map<String, String> map, String... keys) {
        for (String key : keys) {
            String value = map.get(key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }


    private void updateTransactionIdIfMissing(String orderIdRaw, String orderNumber, String tid) {
        try {
            PaymentInfo payment = null;
            if (orderIdRaw != null && !orderIdRaw.isBlank()) {
                Long orderId = Long.valueOf(orderIdRaw);
                payment = mapper.findLatestPaymentByOrderId(orderId);
            }
            if (payment == null && orderNumber != null && !orderNumber.isBlank()) {
                payment = mapper.findLatestPayment(orderNumber);
            }
            if (payment == null) {
                return;
            }
            String existingTid = payment.getTransactionId();
            if (existingTid != null && !existingTid.isBlank()) {
                return;
            }
            mapper.updatePaymentTransactionId(payment.getPaymentId(), tid);
        } catch (Exception e) {
            log.warn("[PG 콜백] Failed to update transactionId for orderId={}", orderIdRaw, e);
        }
    }
}
