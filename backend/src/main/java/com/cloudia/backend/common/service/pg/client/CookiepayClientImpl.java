package com.cloudia.backend.common.service.pg.client;

import com.cloudia.backend.common.model.pg.PGReadyRequest;
import com.cloudia.backend.common.model.pg.PGResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class CookiepayClientImpl {

    /**
     * ローカル開発用のダミー READY 応答
     */
    public PGResult ready(PGReadyRequest req, Map<String, Object> payload) {
        String tid = generateMockTid();
        log.info("[CookiePay MOCK][READY] orderNo={}, amount={}, payMethod={}, tid={}",
                req != null ? req.getOrderNo() : null,
                req != null ? req.getAmount() : null,
                req != null ? req.getPayMethod() : null,
                tid);

        return PGResult.builder()
                .resultCode("0000")
                .resultMsg("MOCK_READY_OK")
                .tid(tid)
                .orderNo(req != null ? req.getOrderNo() : null)
                .amount(req != null && req.getAmount() != null ? String.valueOf(req.getAmount()) : null)
                .payMethod(req != null ? req.getPayMethod() : null)
                .url(req != null ? req.getReturnUrl() : null)
                .redirectUrl(req != null ? req.getReturnUrl() : null)
                .html(buildMockReadyHtml(req, tid))
                .pgScripts("")
                .build();
    }

    /**
     * ローカル開発用のダミー APPROVE 応答
     */
    public PGResult approve(Map<String, Object> payload) {
        String tid = firstNonBlank(payload, "tid", "TID", "transactionId");
        if (tid == null || tid.isBlank()) {
            tid = generateMockTid();
        }
        String amount = extractAmount(payload);

        log.info("[CookiePay MOCK][APPROVE] tid={}, amount={}", tid, amount);

        return PGResult.builder()
                .resultCode("0000")
                .resultMsg("MOCK_APPROVE_OK")
                .tid(tid)
                .amount(amount)
                .acceptNo("MOCK-ACCEPT")
                .acceptDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                .build();
    }

    /**
     * ローカル開発用のダミー CANCEL 応答
     */
    public PGResult cancel(Map<String, Object> payload) {
        String tid = firstNonBlank(payload, "tid", "TID", "transactionId");
        String amount = extractAmount(payload);
        log.info("[CookiePay MOCK][CANCEL] tid={}, amount={}", tid, amount);

        return PGResult.builder()
                .resultCode("0000")
                .resultMsg("MOCK_CANCEL_OK")
                .tid(tid)
                .amount(amount)
                .build();
    }

    /**
     * ローカル開発用のダミー decrypt 応答
     */
    public PGResult decrypt(Map<String, Object> payload) {
        log.info("[CookiePay MOCK][DECRYPT]");
        return PGResult.builder()
                .resultCode("0000")
                .resultMsg("MOCK_DECRYPT_OK")
                .build();
    }

    /**
     * ローカル開発用のダミー payCert 応答
     */
    public PGResult payCert(Map<String, Object> payload) {
        String tid = firstNonBlank(payload, "tid", "TID", "transactionId");
        String amount = extractAmount(payload);
        log.info("[CookiePay MOCK][PAYCERT] tid={}, amount={}", tid, amount);

        return PGResult.builder()
                .resultCode("0000")
                .resultMsg("MOCK_PAYCERT_OK")
                .tid(tid)
                .amount(amount)
                .build();
    }

    /**
     * ローカル開発用のダミー receipt 応答
     */
    public PGResult receipt(Map<String, Object> payload) {
        String tid = firstNonBlank(payload, "tid", "TID", "transactionId");
        log.info("[CookiePay MOCK][RECEIPT] tid={}", tid);
        return PGResult.builder()
                .resultCode("0000")
                .resultMsg("MOCK_RECEIPT_OK")
                .tid(tid)
                .build();
    }

    private String generateMockTid() {
        String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder suffix = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            suffix.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return prefix + "GU00" + suffix;
    }

    private String firstNonBlank(Map<String, Object> payload, String... keys) {
        if (payload == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Object value = payload.get(key);
            if (value != null) {
                String text = String.valueOf(value).trim();
                if (!text.isBlank()) {
                    return text;
                }
            }
        }
        return null;
    }

    private String extractAmount(Map<String, Object> payload) {
        String amount = firstNonBlank(payload, "amount", "AMOUNT");
        return amount != null ? amount : "0";
    }

    private String buildMockReadyHtml(PGReadyRequest req, String tid) {
        String orderNo = req != null ? req.getOrderNo() : "";
        String amount = req != null && req.getAmount() != null ? String.valueOf(req.getAmount()) : "0";
        return "<!doctype html><html><head><meta charset=\"utf-8\"/>"
                + "<title>CookiePay Mock</title></head><body>"
                + "<h3>CookiePay Mock READY</h3>"
                + "<p>orderNo: " + orderNo + "</p>"
                + "<p>amount: " + amount + "</p>"
                + "<p>tid: " + tid + "</p>"
                + "</body></html>";
    }
}
