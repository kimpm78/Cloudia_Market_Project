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
    private String API_ID; // 쿠키페이 결제 연동 key

    @Value("${cookiepay.secret-key}")
    private String API_KEY; // 쿠키페이 결제 연동 key

    @Value("${cookiepay.api.domain:https://sandbox.cookiepayments.com}")
    private String API_DOMAIN;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CM901052Service cm901052Service;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/test")
    public String getMethodName() {
        try {
            // 1. 토큰 발행 요청
            String token = getAuthToken();
            if (token == null) {
                return null;
            }

            // 2. 결제 취소 요청
            String cancelUrl = API_DOMAIN + "/api/cancel";

            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("ApiKey", API_KEY);
            headers.set("TOKEN", token);

            // 요청 바디 설정
            Map<String, String> cancelData = new HashMap<>();
            cancelData.put("tid", "260131144253GU00KCCV");
            cancelData.put("reason", "고객변심");
            HttpEntity<Map<String, String>> cancelRequest = new HttpEntity<>(cancelData, headers);

            // API 호출
            ResponseEntity<String> cancelResponse = restTemplate.exchange(
                    cancelUrl,
                    HttpMethod.POST,
                    cancelRequest,
                    String.class);

            // 응답 파싱
            JsonNode cancelResult = objectMapper.readTree(cancelResponse.getBody());
            log.info("결제 취소 응답: {}", cancelResult);

            Map<String, Object> result = new HashMap<>();
            result.put("result", true);
            result.put("data", cancelResult);

            return new String();
        } catch (Exception e) {
            log.error("결제 취소 중 오류 발생", e);
            return null;
        }
    }

    /**
     * 쿠키페이 인증 토큰 발행
     */
    private String getAuthToken() {
        try {
            String tokenUrl = API_DOMAIN + "/payAuth/token";

            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 요청 바디 설정
            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("pay2_id", API_ID);
            tokenData.put("pay2_key", API_KEY);

            HttpEntity<Map<String, String>> tokenRequest = new HttpEntity<>(tokenData, headers);

            // API 호출
            ResponseEntity<String> tokenResponse = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    tokenRequest,
                    String.class);

            // 응답 파싱
            JsonNode tokenResult = objectMapper.readTree(tokenResponse.getBody());
            String rtnCd = tokenResult.get("RTN_CD").asText();

            // 0000: 성공
            if ("0000".equals(rtnCd)) {
                return tokenResult.get("TOKEN").asText();
            } else {
                log.error("토큰 발행 실패: RTN_CD={}", rtnCd);
                return null;
            }

        } catch (Exception e) {
            log.error("토큰 발행 중 오류 발생", e);
            return null;
        }
    }

    /**
     * 환불/교환 리스트 조회
     * 
     * @return 환불/교환 리스트
     */
    @GetMapping("/findByPeriod")

    public ResponseEntity<ResponseModel<List<ReturnsDto>>> getPeriod(RefundSearchRequestDto searchDto) {
        List<ReturnsDto> result = cm901052Service.getPeriod(searchDto);
        return ResponseEntity.ok(ResponseHelper.success(result, "조회 성공"));
    }

    /**
     * 환불/교환 리스트 조회
     * 
     * @return 환불/교환 리스트
     */
    @GetMapping("/findAll")
    public ResponseEntity<ResponseModel<List<ReturnsDto>>> getRefund() {
        List<ReturnsDto> result = cm901052Service.getRefund();
        return ResponseEntity.ok(ResponseHelper.success(result, "조회 성공"));
    }

    /**
     * 환불 상품 리스트
     * 
     * @param requestNo    요청 번호
     * @param refundNumber 사원 번호
     * @param orderNumber  주문 번호
     * @return 환불 상품 리스트
     */
    @GetMapping("/orderDetail")
    public ResponseEntity<ResponseModel<List<OrderDetailDto>>> getOrderDetail(@RequestParam String requestNo,
            @RequestParam String refundNumber,
            @RequestParam String orderNumber) {
        List<OrderDetailDto> result = cm901052Service.getOrderDetail(requestNo, refundNumber, orderNumber);
        return ResponseEntity.ok(ResponseHelper.success(result, "조회 성공"));
    }

    /**
     * 환불 진행 처리
     * 
     * @param requestNo 환불 정보
     * @return 환불 진행 업데이트
     */
    @PostMapping("/process")
    public ResponseEntity<ResponseModel<Integer>> updateRefund(@RequestBody RefundRequestDto requestDto,
            HttpServletRequest request) {
        String userId = jwtTokenProvider
                .getUserIdFromToken(jwtTokenProvider.resolveToken(request))
                .toString();
        Integer result = cm901052Service.updateRefund(requestDto, userId);
        return ResponseEntity.ok(ResponseHelper.success(result, "업데이트 성공"));
    }
}
