package com.cloudia.backend.CM_06_1001.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.cloudia.backend.CM_01_1001.mapper.CM011001UserMapper;
import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_06_1001.constants.CM061001MessageConstant;
import com.cloudia.backend.CM_06_1001.model.OrderCreate;
import com.cloudia.backend.CM_06_1001.model.OrderSummary;
import com.cloudia.backend.CM_06_1001.service.CM061001Service;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.constants.CMMessageConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/guest")
@RequiredArgsConstructor
@Slf4j
public class CM061001Controller {

    private final CM061001Service cm061001Service;
    private final CM011001UserMapper cm011001UserMapper;

    /**
     * 로그인 사용자 정보 조회
     */
    private User getLoginUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        User user = cm011001UserMapper.findByLoginId(authentication.getName());
        if (user == null || user.getMemberNumber() == null) {
            return null;
        }

        return user;
    }

    /**
     * 주문 생성 (로그인 필수)
     *
     * @param request 주문 생성 요청
     */
    @PostMapping("/order/create")
    public ResponseEntity<ResponseModel<OrderSummary>> createOrder(
            @RequestBody OrderCreate request,
            Authentication authentication) {

        try {
            User user = getLoginUser(authentication);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(buildResponse(null, false,CMMessageConstant.FAIL_UNAUTHORIZED));
            }

            request.setMemberNumber(user.getMemberNumber());
            final String auditUserId =
                    user.getUserId() != null ? String.valueOf(user.getUserId()) : user.getLoginId();
            request.setCreatedBy(auditUserId);
            request.setUpdatedBy(auditUserId);
            request.setUserId(user.getUserId() != null ? user.getUserId().longValue() : null);
            OrderSummary summary = cm061001Service.createOrder(request);

            return ResponseEntity.ok(
                    buildResponse(summary, true, CM061001MessageConstant.ORDER_CREATE_SUCCESS)
            );

        } catch (Exception e) {
            log.error("[ORDER][CREATE] ERROR", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(null, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 주문 완료 처리 (PG 승인 후)
     */
    @PostMapping("/order/complete")
    public ResponseEntity<ResponseModel<Void>> completeOrder(
            @RequestParam Long orderId,
            Authentication authentication) {

        try {
            User user = getLoginUser(authentication);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(buildResponse(null, false, CMMessageConstant.FAIL_UNAUTHORIZED));
            }

            cm061001Service.completeOrder(orderId, user.getMemberNumber());

            return ResponseEntity.ok(
                    buildResponse(null, true, CM061001MessageConstant.ORDER_COMPLETE_SUCCESS)
            );

        } catch (Exception e) {
            log.error("[ORDER][COMPLETE] ERROR", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(null, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 주문 조회 (본인 주문만 가능)
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ResponseModel<OrderSummary>> getOrderSummary(
            @PathVariable Long orderId,
            Authentication authentication) {

        try {
            User user = getLoginUser(authentication);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(buildResponse(null, false, CM061001MessageConstant.ORDER_FORBIDDEN));
            }

            OrderSummary summary = cm061001Service.getOrderSummary(orderId, user.getMemberNumber());

            return ResponseEntity.ok(
                    buildResponse(summary, true, CM061001MessageConstant.ORDER_FETCH_SUCCESS)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildResponse(null, false, CM061001MessageConstant.ORDER_FETCH_FAIL));

        } catch (Exception e) {
            log.error("[ORDER][DETAIL] ERROR", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(null, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 공통 응답 모델 생성
     * 
     * @param resultList 결과 데이터
     * @param result     처리 결과
     * @param message    응답 메시지
     * @return ResponseModel 객체
     */
    private <T> ResponseModel<T> buildResponse(T data, boolean result, String msg) {
        return ResponseModel.<T>builder()
                .result(result)
                .message(msg)
                .resultList(data)
                .build();
    }
}
