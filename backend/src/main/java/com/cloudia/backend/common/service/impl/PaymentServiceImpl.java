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
     * PG ready 呼び出し
     *
     * @param request PG準備リクエストオブジェクト
     * @return PG準備結果および注文情報
     */
    @Override
    public ResponseModel<Map<String, Object>> ready(PGReadyRequest request) {
        try {
            validateReadyRequest(request);

            final Long orderId = request.getOrderId();
            final String pgType = request.getPgType();

            // PGのORDERNOはサーバー側でグローバルに一意生成
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

            log.info("[決済準備リクエスト] {}", request);

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
                    .message("PG準備エラー: " + e.getMessage())
                    .resultList(null)
                    .build();
        }
    }

    /**
     * 承認リクエスト + DB更新
     *
     * @param request PG承認リクエストオブジェクト
     * @return PG承認結果および関連情報
     */
    @Override
    @Transactional
    public ResponseModel<Map<String, Object>> approve(PGApproveRequest request) {
        try {
            validateApproveRequest(request);

            String pgType = resolvePgType(request.getPgType());
            request.setPgType(pgType);

            // tid で決済を1件特定（なければ orderId / orderNumber で補完）
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
            // PG応答の amount が文字列の場合があるため、数字のみで比較
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

            // （成功時）在庫処理／カート無効化は orderId 基準
            String paymentUserId = resolvePaymentUserId(orderId);

            List<OrderItemInfo> orderItems = null;
            if (success) {
                mapper.deactivateCartItemsByOrderId(orderId, paymentUserId);

                orderItems = mapper.findOrderItems(orderId);
                if (orderItems == null || orderItems.isEmpty()) {
                    throw new IllegalStateException("注文商品の情報が見つかりません。");
                }

                for (OrderItemInfo item : orderItems) {
                    String productId = item.getProductId();
                    int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                    if (productId == null || productId.isBlank() || quantity < 1) {
                        throw new IllegalStateException("在庫差し引き対象情報が正しくありません。");
                    }

                    int affected = mapper.decreaseStock(productId, quantity);
                    if (affected != 1) {
                        throw new IllegalStateException("在庫不足のため注文を完了できません。");
                    }

                    Long stockId = mapper.findStockIdByProductCode(productId);
                    if (stockId == null) {
                        throw new IllegalStateException("在庫情報が見つかりません。");
                    }

                    mapper.insertStockDetail(stockId, -1L * quantity, null, paymentUserId, paymentUserId);
                }
            }

            // DB 更新は tid 基準で
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
                    .message("PG承認中にエラーが発生しました: " + e.getMessage())
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
            return "銀行振込";
        }
        if ("CARD".equalsIgnoreCase(paymentMethod)) {
            return "クレジットカード";
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
     * キャンセル — PGキャンセル + DB更新
     * 
     * @param request PGキャンセルリクエストオブジェクト
     * @return PGキャンセル結果および関連情報
     */
    @Override
    @Transactional
    public ResponseModel<Map<String, Object>> cancel(PGCancelRequest request) {
        try {
            log.info("[決済キャンセル] 開始： orderId={}, tid={}", request.getOrderId(), request.getTid());

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
                    .message("PGキャンセル中のエラー発生: " + e.getMessage())
                    .resultList(null)
                    .build();
        }
    }

    /**
     * FAIL — 内部失敗処理（ユーザー閉じる／PG失敗リダイレクトなど）
     * 
     * @param request 失敗 処理 リクエスト オブジェクト
     * @return 処理結果
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
                throw new IllegalArgumentException("決済情報が見つかりません。");
            }

            log.info(
                    "[PG FAIL LOOKUP] paymentId={}, orderId={}, orderNumber={}, tid={}",
                    payment.getPaymentId(),
                    payment.getOrderId(),
                    payment.getOrderNumber(),
                    payment.getTransactionId());

            String status = String.valueOf(payment.getPaymentStatusCode());

            // 013: 2=承認, 3=失敗（コード一覧基準）
            if ("2".equals(status)) {
                return ResponseModel.<Map<String, Object>>builder()
                        .result(true)
                        .message("既に承認済みの決済です。")
                        .resultList(Map.of("success", true, "idempotent", true))
                        .build();
            }
            if ("3".equals(status)) {
                return ResponseModel.<Map<String, Object>>builder()
                        .result(true)
                        .message("既に失敗処理済みの決済です。")
                        .resultList(Map.of("success", true, "idempotent", true))
                        .build();
            }

            final String reason = (request.getReason() != null && !request.getReason().isBlank())
                    ? request.getReason()
                    : "ユーザーまたはPG処理失敗";

            // tid 基準で更新
            mapper.updatePaymentStatusToFailed(
                    payment.getPaymentId(),
                    tid,
                    "FAIL",
                    reason,
                    resolvePaymentUserId(payment.getOrderId()),
                    dateCalculator.tokyoTime());

            // 注文ステータス更新は payment.orderId を使用
            mapper.updateOrderStatusOnApprove(
                    payment.getOrderId(),
                    8,
                    reason,
                    resolvePaymentUserId(payment.getOrderId()));

            return ResponseModel.<Map<String, Object>>builder()
                    .result(true)
                    .message("決済を失敗として処理しました。")
                    .resultList(Map.of("success", true))
                    .build();

        } catch (Exception e) {
            log.error("[PG FAIL ERROR]", e);
            return ResponseModel.<Map<String, Object>>builder()
                    .result(false)
                    .message("PG失敗処理中にエラーが発生しました: " + e.getMessage())
                    .resultList(null)
                    .build();
        }
    }

    /**
     * DECRYPT — PG encData 復号
     *
     * @param request 復号リクエストオブジェクト
     * @return 復号結果および関連情報
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
                    .message("PG decrypt 中にエラーが発生しました: " + e.getMessage())
                    .resultList(null)
                    .build();
        }
    }

    /**
     * CALLBACK — PG returnURL 復号処理
     *
     * @param params PGコールバックパラメータ
     * @return 復号結果および関連情報
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
                    .message("PG callback 中にエラーが発生しました: " + e.getMessage())
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
            throw new IllegalArgumentException("リクエスト値が空です。");
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
            log.warn("[PGコールバック] transactionId の更新に失敗しました。orderId={}", orderIdRaw, e);
        }
    }
}
