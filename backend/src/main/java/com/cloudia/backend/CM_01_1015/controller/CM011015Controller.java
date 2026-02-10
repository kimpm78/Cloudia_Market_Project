package com.cloudia.backend.CM_01_1015.controller;

import com.cloudia.backend.CM_01_1015.constants.CM011015MessageConstant;
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
     * 交換・返品申請履歴一覧取得
     */
    @GetMapping
    public ResponseEntity<ResponseModel<List<ReturnResponse>>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null)
            return ResponseEntity.status(401).build();

        String loginId = userDetails.getUsername();
        log.info(CM011015MessageConstant.LOG_RETURN_HISTORY_REQUEST, loginId);

        ResponseModel<List<ReturnResponse>> response = cm011015Service.getReturnHistory(loginId);
        return ResponseEntity.ok(response);
    }

    /**
     * 交換・返品申請詳細取得
     */
    @GetMapping("/{returnId}")
    public ResponseEntity<ResponseModel<ReturnResponse>> getReturnDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int returnId) {

        if (userDetails == null)
            return ResponseEntity.status(401).build();

        String loginId = userDetails.getUsername();
        log.info(CM011015MessageConstant.LOG_RETURN_DETAIL_REQUEST, returnId, loginId);

        ResponseModel<ReturnResponse> response = cm011015Service.getReturnDetail(loginId, returnId);
        return ResponseEntity.ok(response);
    }

    /**
     * 交換・返品の統合申請リクエスト
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseModel<Object>> createReturnRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute ReturnRequest request) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(
                    ResponseModel.builder().result(false).message("ログインが必要です。").build());
        }

        String loginId = userDetails.getUsername();
        log.info(CM011015MessageConstant.LOG_RETURN_CREATE_REQUEST, loginId, request.getTitle());

        return cm011015Service.createReturnRequest(loginId, request);
    }

    /**
     * 詳細取得
     */
    @GetMapping("/order-products")
    public ResponseEntity<List<ReturnResponse.ProductInfo>> getOrderProducts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("orderNo") String orderNo) {

        if (userDetails == null)
            return ResponseEntity.status(401).build();

        String loginId = userDetails.getUsername();
        log.info(CM011015MessageConstant.LOG_ORDER_PRODUCTS_REQUEST, orderNo, loginId);

        return cm011015Service.getOrderProducts(loginId, orderNo);
    }

    /**
     * 交換・返品申請可能な購入確定注文一覧取得
     */
    @GetMapping("/returnable")
    public ResponseEntity<ResponseModel<List<Map<String, Object>>>> getReturnableOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null)
            return ResponseEntity.status(401).build();

        String loginId = userDetails.getUsername();
        log.info(CM011015MessageConstant.LOG_RETURNABLE_ORDERS_REQUEST, loginId);

        ResponseModel<List<Map<String, Object>>> response = cm011015Service.getReturnableOrderList(loginId);
        return ResponseEntity.ok(response);
    }

}
