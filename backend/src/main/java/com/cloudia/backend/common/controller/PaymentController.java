package com.cloudia.backend.common.controller;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.model.pg.PGApproveRequest;
import com.cloudia.backend.common.model.pg.PGCancelRequest;
import com.cloudia.backend.common.model.pg.PGDecryptRequest;
import com.cloudia.backend.common.model.pg.PGFailRequest;
import com.cloudia.backend.common.model.pg.PGReadyRequest;
import com.cloudia.backend.common.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * PG 결제 준비 (Ready)
     */
    @PostMapping("/ready")
    public ResponseModel<Map<String, Object>> ready(@RequestBody PGReadyRequest request) {
        return paymentService.ready(request);
    }

    /**
     * PG 결제 승인 (Approve)
     */
    @PostMapping("/approve")
    public ResponseModel<Map<String, Object>> approve(@RequestBody PGApproveRequest request) {
        return paymentService.approve(request);
    }

    /**
     * PG 결제 취소 (Cancel - 사용자/관리자)
     */
    @PostMapping("/cancel")
    public ResponseModel<Map<String, Object>> cancel(@RequestBody PGCancelRequest request) {
        return paymentService.cancel(request);
    }

    /**
     * PG 결제 실패/닫힘 처리 (내부 상태만 업데이트)
     */
    @PostMapping("/fail")
    public ResponseModel<Map<String, Object>> fail(@RequestBody PGFailRequest request) {
        return paymentService.fail(request);
    }

    /**
     * 통합 PG Callback 처리 (Success, Cancel, Fail 통합)
     * 
     * @param success, cancel, fail 중 하나
     * @param allParams 모든 쿼리 파라미터
     * @return 리다이렉트 응답
     */
    @GetMapping("/callback")
    @RequestMapping(value = {"/callback", "/callback/{type}"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Void> handleCookiePayCallback(
            @PathVariable(required = false) String type,
            @RequestParam Map<String, String> allParams) {

        // 쿠키페이는 성공/실패 여부를 'resultStatus' 파라미터로 보내줍니다.
        String status = (type != null) ? type : allParams.get("resultStatus");
        
        log.info("[COOKIEPAY CALLBACK] Status: {}, OrderId: {}, Message: {}", 
                status, allParams.get("orderId"), allParams.get("resultMessage"));

        // 서비스 로직 수행
        ResponseModel<Map<String, Object>> rm = paymentService.callback(allParams);

        // 쿠키페이는 결제 성공 시 결제 완료 페이지로, 실패 시(DECLINE) 에러 페이지나 메인으로 리다이렉트
        String redirectUrl = extractRedirectUrl(rm, status);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }

    private String extractRedirectUrl(ResponseModel<Map<String, Object>> rm, String status) {
        if (rm != null && rm.getResultList() != null && rm.getResultList().containsKey("redirectUrl")) {
            return String.valueOf(rm.getResultList().get("redirectUrl"));
        }
        
        // 로직 처리가 애매할 때 기본 리다이렉트 경로
        if ("DECLINE".equals(status)) {
            return "/payment/fail"; // 결제 실패 페이지
        }
        return "/payment/success"; // 결제 성공 페이지
    }

    /**
     * PG encData 복호화
     */
    @GetMapping("/decrypt")
    public ResponseModel<Map<String, Object>> decrypt(@RequestParam Map<String, String> query) {
        PGDecryptRequest request = new PGDecryptRequest();
        request.setEncData(query.get("encData"));
        request.setPgType(query.getOrDefault("pgType", "COOKIEPAY"));
        return paymentService.decrypt(request);
    }
}
