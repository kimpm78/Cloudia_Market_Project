package com.cloudia.backend.CM_01_1005.controller;

import com.cloudia.backend.CM_01_1005.constants.CM011005MessageConstant;
import com.cloudia.backend.CM_01_1005.model.OrderDetailResponse;
import com.cloudia.backend.CM_01_1005.model.OrderListResponse;
import com.cloudia.backend.CM_01_1005.service.CM011005Service;
import com.cloudia.backend.CM_01_1005.model.OrderCancelRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/mypage")
@RequiredArgsConstructor
@Slf4j
public class CM011005Controller {

    private final CM011005Service CM011005Service;

    @GetMapping("/purchases")
    public ResponseEntity<List<OrderListResponse>> getOrderHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer orderStatusValue,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer paymentMethod) {

        String loginId = userDetails.getUsername();

        Map<String, Object> filters = new HashMap<>();
        filters.put("keyword", keyword);
        filters.put("orderStatusValue", orderStatusValue);
        filters.put("year", year);
        filters.put("month", month);
        filters.put("paymentMethod", paymentMethod);

        log.info(CM011005MessageConstant.CONTROLLER_GET_HISTORY_START, loginId, filters);

        ResponseEntity<List<OrderListResponse>> response = CM011005Service.searchOrderHistory(loginId, filters);

        long count = response.getBody() != null ? response.getBody().size() : 0;
        log.info(CM011005MessageConstant.CONTROLLER_GET_HISTORY_END, count, loginId);

        return response;
    }

    @GetMapping("/purchases/{orderNo}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderNo) {

        String loginId = userDetails.getUsername();
        return CM011005Service.getOrderDetail(loginId, orderNo);
    }

    @PostMapping("/purchases/{orderNo}/cancel")
    public ResponseEntity<String> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderNo,
            @RequestBody OrderCancelRequest request) {

        String loginId = userDetails.getUsername();
        request.setOrderNo(orderNo);

        return CM011005Service.cancelOrder(loginId, request);
    }

    @GetMapping("/delivery-addresses")
    public ResponseEntity<List<Map<String, Object>>> getDeliveryAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {

        String loginId = userDetails.getUsername();
        return CM011005Service.getDeliveryAddresses(loginId);
    }

    @PutMapping("/purchases/{orderNo}/shipping")
    public ResponseEntity<String> updateShippingInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderNo,
            @RequestBody Map<String, Object> params) {

        String loginId = userDetails.getUsername();
        params.put("orderNo", orderNo);

        return CM011005Service.updateShippingInfo(loginId, params);
    }
}