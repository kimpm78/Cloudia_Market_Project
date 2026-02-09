package com.cloudia.backend.CM_01_1015.controller;

import com.cloudia.backend.CM_01_1015.model.ReturnResponse;
import com.cloudia.backend.CM_01_1015.model.ReturnRequest;
import com.cloudia.backend.CM_01_1015.service.CM011015Service;
import com.cloudia.backend.common.model.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/returns")
@RequiredArgsConstructor
@Slf4j
public class CM011015Controller {

    private final CM011015Service cm011015Service;

    /**
     * 교환/반품 신청 내역 목록 조회
     */
    @GetMapping
    public ResponseEntity<ResponseModel<List<ReturnResponse>>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null)
            return ResponseEntity.status(401).build();

        String loginId = userDetails.getUsername();
        log.info("교환/반품 내역 목록 조회 요청 - 사용자: {}", loginId);

        ResponseModel<List<ReturnResponse>> response = cm011015Service.getReturnHistory(loginId);
        return ResponseEntity.ok(response);
    }

    /**
     * 교환/반품 상세 내역 조회
     */
    @GetMapping("/{returnId}")
    public ResponseEntity<ResponseModel<ReturnResponse>> getReturnDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int returnId) {

        if (userDetails == null)
            return ResponseEntity.status(401).build();

        String loginId = userDetails.getUsername();
        log.info("교환/반품 상세 조회 요청 - ID: {}, 사용자: {}", returnId, loginId);

        ResponseModel<ReturnResponse> response = cm011015Service.getReturnDetail(loginId, returnId);
        return ResponseEntity.ok(response);
    }

    /**
     * 교환/반품 통합 신청 요청
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseModel<Object>> createReturnRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute ReturnRequest request) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(
                    ResponseModel.builder().result(false).message("로그인이 필요합니다.").build());
        }

        String loginId = userDetails.getUsername();
        log.info("신규 교환/반품 신청 접수 - 사용자: {}, 제목: {}", loginId, request.getTitle());

        return cm011015Service.createReturnRequest(loginId, request);
    }

    /**
     * 상세 조회
     */
    @GetMapping("/order-products")
    public ResponseEntity<List<ReturnResponse.ProductInfo>> getOrderProducts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("orderNo") String orderNo) {

        if (userDetails == null)
            return ResponseEntity.status(401).build();

        String loginId = userDetails.getUsername();
        log.info("신청용 주문 상품 조회 요청 - 주문번호: {}, 사용자: {}", orderNo, loginId);

        return cm011015Service.getOrderProducts(loginId, orderNo);
    }

    /**
     * 교환/반품 신청 가능한 구매 확정 주문 목록 조회
     */
    @GetMapping("/returnable")
    public ResponseEntity<ResponseModel<List<Map<String, Object>>>> getReturnableOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null)
            return ResponseEntity.status(401).build();

        String loginId = userDetails.getUsername();
        log.info("신청 가능한 주문 목록 조회 요청 - 사용자: {}", loginId);

        ResponseModel<List<Map<String, Object>>> response = cm011015Service.getReturnableOrderList(loginId);
        return ResponseEntity.ok(response);
    }

}