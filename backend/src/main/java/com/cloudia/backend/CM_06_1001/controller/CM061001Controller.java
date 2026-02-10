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
     * ログインユーザー情報の取得
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
     * 注文作成（ログイン必須）
     *
     * @param request 注文作成リクエスト
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
     * 注文完了処理（PG承認後）
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
     * 注文取得（本人の注文のみ可）
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
     * 共通レスポンスモデル生成
     *
     * @param resultList 結果データ
     * @param result     処理結果
     * @param message    応答メッセージ
     * @return ResponseModel オブジェクト
     */
    private <T> ResponseModel<T> buildResponse(T data, boolean result, String msg) {
        return ResponseModel.<T>builder()
                .result(result)
                .message(msg)
                .resultList(data)
                .build();
    }
}
