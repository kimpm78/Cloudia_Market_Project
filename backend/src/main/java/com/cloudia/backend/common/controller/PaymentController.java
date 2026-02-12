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
     * PG決済準備（Ready）
     */
    @PostMapping("/ready")
    public ResponseModel<Map<String, Object>> ready(@RequestBody PGReadyRequest request) {
        return paymentService.ready(request);
    }

    /**
     * PG決済承認（Approve）
     */
    @PostMapping("/approve")
    public ResponseModel<Map<String, Object>> approve(@RequestBody PGApproveRequest request) {
        return paymentService.approve(request);
    }

    /**
     * PG決済キャンセル（Cancel - ユーザー／管理者）
     */
    @PostMapping("/cancel")
    public ResponseModel<Map<String, Object>> cancel(@RequestBody PGCancelRequest request) {
        return paymentService.cancel(request);
    }

    /**
     * PG決済失敗／クローズ処理（内部状態のみ更新）
     */
    @PostMapping("/fail")
    public ResponseModel<Map<String, Object>> fail(@RequestBody PGFailRequest request) {
        return paymentService.fail(request);
    }

    /**
     * 統合PG Callback処理（Success, Cancel, Fail を統合）
     *
     * @param type success / cancel / fail のいずれか
     * @param allParams すべてのクエリパラメータ
     * @return リダイレクトレスポンス
     */
    @GetMapping("/callback")
    @RequestMapping(value = {"/callback", "/callback/{type}"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Void> handleCookiePayCallback(
            @PathVariable(required = false) String type,
            @RequestParam Map<String, String> allParams) {

        // CookiePay は成功／失敗の判定を 'resultStatus' パラメータで返却します。
        String status = (type != null) ? type : allParams.get("resultStatus");
        
        log.info("[COOKIEPAY CALLBACK] Status: {}, OrderId: {}, Message: {}", 
                status, allParams.get("orderId"), allParams.get("resultMessage"));

        // サービスロジック実行
        ResponseModel<Map<String, Object>> rm = paymentService.callback(allParams);

        // CookiePay は決済成功時は完了ページへ、失敗（DECLINE）時はエラーページまたはメインへリダイレクトします。
        String redirectUrl = extractRedirectUrl(rm, status);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }

    private String extractRedirectUrl(ResponseModel<Map<String, Object>> rm, String status) {
        if (rm != null && rm.getResultList() != null && rm.getResultList().containsKey("redirectUrl")) {
            return String.valueOf(rm.getResultList().get("redirectUrl"));
        }
        
        // ロジック判定が曖昧な場合のデフォルトリダイレクト先
        if ("DECLINE".equals(status)) {
            return "/payment/fail"; // 決済失敗ページ
        }
        return "/payment/success"; // 決済成功ページ
    }

    /**
     * PG encData の復号
     */
    @GetMapping("/decrypt")
    public ResponseModel<Map<String, Object>> decrypt(@RequestParam Map<String, String> query) {
        PGDecryptRequest request = new PGDecryptRequest();
        request.setEncData(query.get("encData"));
        request.setPgType(query.getOrDefault("pgType", "COOKIEPAY"));
        return paymentService.decrypt(request);
    }
}
