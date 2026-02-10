package com.cloudia.backend.CM_90_1052.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cloudia.backend.CM_90_1052.model.OrderDetailDto;
import com.cloudia.backend.CM_90_1052.model.RefundRequestDto;
import com.cloudia.backend.CM_90_1052.model.RefundSearchRequestDto;
import com.cloudia.backend.CM_90_1052.model.ReturnsDto;
import com.cloudia.backend.CM_90_1052.service.CM901052Service;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.util.ResponseHelper;
import com.cloudia.backend.config.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/settlement/refund")
public class CM901052Controller {

    @Value("${cookiepay.store-id}")
    private String API_ID;

    @Value("${cookiepay.secret-key}")
    private String API_KEY;

    @Value("${cookiepay.api.domain:https://sandbox.cookiepayments.com}")
    private String API_DOMAIN;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CM901052Service cm901052Service;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/test")
    public String getMethodName() {
        try {
            // 1. トークン発行リクエスト
            String token = getAuthToken();
            if (token == null) {
                return null;
            }

            // 2. 決済取消リクエスト
            String cancelUrl = API_DOMAIN + "/api/cancel";

            // リクエストヘッダー設定
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("ApiKey", API_KEY);
            headers.set("TOKEN", token);

            // リクエストボディ設定
            Map<String, String> cancelData = new HashMap<>();
            cancelData.put("tid", "260131144253GU00KCCV");
            cancelData.put("reason", "お客様都合");
            HttpEntity<Map<String, String>> cancelRequest = new HttpEntity<>(cancelData, headers);

            // API呼び出し
            ResponseEntity<String> cancelResponse = restTemplate.exchange(
                    cancelUrl,
                    HttpMethod.POST,
                    cancelRequest,
                    String.class);

            // レスポンス解析
            JsonNode cancelResult = objectMapper.readTree(cancelResponse.getBody());
            log.info("決済取消レスポンス: {}", cancelResult);

            Map<String, Object> result = new HashMap<>();
            result.put("result", true);
            result.put("data", cancelResult);

            return new String();
        } catch (Exception e) {
            log.error("決済取消中にエラーが発生しました", e);
            return null;
        }
    }

    /**
     * CookiePay 認証トークン発行
     */
    private String getAuthToken() {
        try {
            String tokenUrl = API_DOMAIN + "/payAuth/token";

            // リクエストヘッダー設定
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // リクエストボディ設定
            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("pay2_id", API_ID);
            tokenData.put("pay2_key", API_KEY);

            HttpEntity<Map<String, String>> tokenRequest = new HttpEntity<>(tokenData, headers);

            // API呼び出し
            ResponseEntity<String> tokenResponse = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    tokenRequest,
                    String.class);

            // レスポンス解析
            JsonNode tokenResult = objectMapper.readTree(tokenResponse.getBody());
            String rtnCd = tokenResult.get("RTN_CD").asText();

            // 0000: 成功
            if ("0000".equals(rtnCd)) {
                return tokenResult.get("TOKEN").asText();
            } else {
                log.error("トークン発行失敗: RTN_CD={}", rtnCd);
                return null;
            }

        } catch (Exception e) {
            log.error("トークン発行中にエラーが発生しました", e);
            return null;
        }
    }

    /**
     * 返金・交換一覧取得
     * 
     * @return 返金・交換一覧
     */
    @GetMapping("/findByPeriod")

    public ResponseEntity<ResponseModel<List<ReturnsDto>>> getPeriod(RefundSearchRequestDto searchDto) {
        List<ReturnsDto> result = cm901052Service.getPeriod(searchDto);
        return ResponseEntity.ok(ResponseHelper.success(result, "取得成功"));
    }

    /**
     * 返金・交換一覧取得
     * 
     * @return 返金・交換一覧
     */
    @GetMapping("/findAll")
    public ResponseEntity<ResponseModel<List<ReturnsDto>>> getRefund() {
        List<ReturnsDto> result = cm901052Service.getRefund();
        return ResponseEntity.ok(ResponseHelper.success(result, "取得成功"));
    }

    /**
     * 返金商品一覧
     * 
     * @param requestNo    リクエスト番号
     * @param refundNumber 社員番号
     * @param orderNumber  注文番号
     * @return 返金商品一覧
     */
    @GetMapping("/orderDetail")
    public ResponseEntity<ResponseModel<List<OrderDetailDto>>> getOrderDetail(@RequestParam String requestNo,
            @RequestParam String refundNumber,
            @RequestParam String orderNumber) {
        List<OrderDetailDto> result = cm901052Service.getOrderDetail(requestNo, refundNumber, orderNumber);
        return ResponseEntity.ok(ResponseHelper.success(result, "取得成功"));
    }

    /**
     * 返金処理
     * 
     * @param requestNo 返金情報
     * @return 返金処理更新
     */
    @PostMapping("/process")
    public ResponseEntity<ResponseModel<Integer>> updateRefund(@RequestBody RefundRequestDto requestDto,
            HttpServletRequest request) {
        String userId = jwtTokenProvider
                .getUserIdFromToken(jwtTokenProvider.resolveToken(request))
                .toString();
        Integer result = cm901052Service.updateRefund(requestDto, userId);
        return ResponseEntity.ok(ResponseHelper.success(result, "更新成功"));
    }
}
