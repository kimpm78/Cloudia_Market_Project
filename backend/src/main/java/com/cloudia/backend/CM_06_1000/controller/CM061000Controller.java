package com.cloudia.backend.CM_06_1000.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_06_1000.model.CartItemResponse;
import com.cloudia.backend.CM_06_1000.constants.CM061000MessageConstant;
import com.cloudia.backend.CM_06_1000.service.CM061000Service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/guest")
public class CM061000Controller {

    private final CM061000Service cm061000Service;

    /**
     * カート取得
     *
     * @param userId ユーザーID
     * @return カートアイテム一覧
     */
    @GetMapping("/cart")
    public ResponseEntity<ResponseModel<List<CartItemResponse>>> getCart(@RequestParam Long userId) {
        log.debug("userId: {}", userId);
        List<CartItemResponse> list = cm061000Service.getCart(userId);
        String msg = (list == null || list.isEmpty())
                ? CM061000MessageConstant.CART_FETCH_EMPTY
                : CM061000MessageConstant.CART_FETCH_SUCCESS;
        return ResponseEntity.ok(setResponseDto(list, true, msg));
    }

    /**
     * カート最終更新日時の取得
     *
     * @param userId ユーザーID
     * @return 最終 cart_updated_at
     */
    @GetMapping("/cart/last-updated")
    public ResponseEntity<ResponseModel<Map<String, Object>>> getCartLastUpdatedAt(
            @RequestParam Long userId) {
        LocalDateTime lastUpdatedAt = cm061000Service.getLastUpdatedAt(userId);
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("lastUpdatedAt", lastUpdatedAt);
        if (lastUpdatedAt != null) {
            long epochMillis = lastUpdatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            payload.put("lastUpdatedAtEpoch", epochMillis);
        }
        return ResponseEntity.ok(setResponseDto(payload, true, CM061000MessageConstant.CART_FETCH_SUCCESS));
    }

    /**
     * カート初回作成日時の取得（TTL基準）
     *
     * @param userId ユーザーID
     * @return 初回 created_at
     */
    @GetMapping("/cart/created-at")
    public ResponseEntity<ResponseModel<Map<String, Object>>> getCartCreatedAt(
            @RequestParam Long userId) {
        LocalDateTime createdAt = cm061000Service.getCreatedAt(userId);
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("createdAt", createdAt);
        if (createdAt != null) {
            long epochMillis = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            payload.put("createdAtEpoch", epochMillis);
        }
        return ResponseEntity.ok(setResponseDto(payload, true, CM061000MessageConstant.CART_FETCH_SUCCESS));
    }

    /**
     * カートを空にする
     *
     * @return 削除結果
     */
    @DeleteMapping("/cart")
    public ResponseEntity<ResponseModel<Void>> clearCart() {
        Long userId = getCurrentUserId();
        try {
            cm061000Service.clearCart(userId);
            return ResponseEntity.ok(
                    setResponseDto(null, true, CM061000MessageConstant.CART_DELETE_SUCCESS));
        } catch (Exception e) {
            log.error(CM061000MessageConstant.CART_DB_ERROR, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(setResponseDto(null, false, CM061000MessageConstant.CART_DELETE_FAIL));
        }
    }

    /**
     * カートに追加
     *
     * @param req           リクエストボディ
     * @param bindingResult バリデーション結果
     * @return 追加結果
     */
    @PostMapping("/cart/add")
    public ResponseEntity<ResponseModel<Map<String, Object>>> addToCart(
            @RequestBody @Valid AddCartRequest req,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = extractErrorMessage(bindingResult);
            log.warn(CM061000MessageConstant.CART_VALIDATION_FAIL, errorMessage);
            return ResponseEntity.badRequest()
                    .body(setResponseDto(null, false, errorMessage));
        }
        Long userId = getCurrentUserId();
        try {
            Long cartItemId = cm061000Service.addToCart(
                    userId,
                    req.getProductId(),
                    req.getQuantity());

            return ResponseEntity.ok(
                    setResponseDto(
                            Map.of("cartItemId", cartItemId),
                            true,
                            CM061000MessageConstant.CART_ADD_SUCCESS));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(setResponseDto(null, false, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(setResponseDto(null, false, e.getMessage()));
        }
    }

    /**
     * 数量変更
     *
     * @param cartItemId カートアイテムID
     * @param req        数量変更リクエスト
     */
    @PatchMapping("/cart/{cartItemId}")
    public ResponseEntity<ResponseModel<Void>> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestBody @Valid UpdateQtyRequest req,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = extractErrorMessage(bindingResult);
            log.warn(CM061000MessageConstant.CART_VALIDATION_FAIL, errorMessage);
            return ResponseEntity.badRequest()
                    .body(setResponseDto(null, false, errorMessage));
        }

        Long userId = getCurrentUserId();

        try {
            cm061000Service.updateQuantity(userId, cartItemId, req.getQuantity());
            return ResponseEntity.ok(
                    setResponseDto(null, true, CM061000MessageConstant.CART_UPDATE_QTY_SUCCESS));

        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn(CM061000MessageConstant.CART_UPDATE_QTY_FAIL, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(setResponseDto(null, false, e.getMessage()));
        } catch (Exception e) {
            log.error(CM061000MessageConstant.CART_DB_ERROR, e);
            return ResponseEntity.internalServerError()
                    .body(setResponseDto(null, false, CM061000MessageConstant.CART_UPDATE_QTY_FAIL));
        }
    }

    /**
     * カートアイテム削除
     *
     * @param cartItemId カートアイテムID
     * @return 削除結果
     */
    @DeleteMapping("/cart/{cartItemId}")
    public ResponseEntity<ResponseModel<Void>> delete(
            @PathVariable Long cartItemId) {

        Long userId = getCurrentUserId();

        try {
            cm061000Service.remove(userId, cartItemId);
            return ResponseEntity.ok(
                    setResponseDto(null, true, CM061000MessageConstant.CART_DELETE_SUCCESS));
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn(CM061000MessageConstant.CART_DELETE_FAIL, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(setResponseDto(null, false, e.getMessage()));
        } catch (Exception e) {
            log.error(CM061000MessageConstant.CART_DB_ERROR, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(setResponseDto(null, false, CM061000MessageConstant.CART_DELETE_FAIL));
        }
    }

    /**
     * 選択したカートアイテムを削除
     *
     * @param req           選択削除リクエスト（cartItemIds）
     * @param bindingResult バリデーション結果
     * @return 削除結果
     */
    @PostMapping("/cart/delete-selected")
    public ResponseEntity<ResponseModel<Void>> deleteSelected(
            @RequestBody @Valid DeleteSelectedCartRequest req,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = extractErrorMessage(bindingResult);
            log.warn(CM061000MessageConstant.CART_VALIDATION_FAIL, errorMessage);
            return ResponseEntity.badRequest()
                    .body(setResponseDto(null, false, errorMessage));
        }

        List<Long> cartItemIds = req.getCartItemIds();
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(setResponseDto(null, false, CM061000MessageConstant.CART_PREPARE_ORDER_EMPTY));
        }

        Long userId = getCurrentUserId();

        try {
            cm061000Service.deleteSelected(userId, cartItemIds);
            return ResponseEntity.ok(
                    setResponseDto(null, true, CM061000MessageConstant.CART_DELETE_SUCCESS));
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn(CM061000MessageConstant.CART_DELETE_FAIL, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(setResponseDto(null, false, e.getMessage()));
        } catch (Exception e) {
            log.error(CM061000MessageConstant.CART_DB_ERROR, e);
            return ResponseEntity.internalServerError()
                    .body(setResponseDto(null, false, CM061000MessageConstant.CART_DELETE_FAIL));
        }
    }

    /**
     * 注文準備
     *
     * @param req           注文準備リクエスト（選択した cartItemIds）
     * @param bindingResult バリデーション結果
     * @return 選択アイテム一覧と合計情報
     */
    @PostMapping("/cart/prepare-order")
    public ResponseEntity<ResponseModel<Map<String, Object>>> prepareOrder(
            @RequestBody @Valid PrepareOrderRequest req,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = extractErrorMessage(bindingResult);
            log.warn(CM061000MessageConstant.CART_VALIDATION_FAIL, errorMessage);
            return ResponseEntity.badRequest()
                    .body(setResponseDto(null, false, errorMessage));
        }

        List<Long> cartItemIds = req.getCartItemIds();
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(setResponseDto(null, false, CM061000MessageConstant.CART_PREPARE_ORDER_EMPTY));
        }

        Long userId = getCurrentUserId();

        try {
            var items = cm061000Service.prepareOrder(userId, cartItemIds);

            int subtotal = items.stream()
                    .mapToInt(CartItemResponse::getLineTotal)
                    .sum();

            int shipping = items.stream()
                    .mapToInt(i -> i.getShippingFee() == null ? 0 : i.getShippingFee())
                    .sum();

            int total = subtotal + shipping;

            return ResponseEntity.ok(
                setResponseDto(
                    Map.of(
                            "items", items,
                            "subtotal", subtotal,
                            "shipping", shipping,
                            "total", total
                    ),
                    true,
                    CM061000MessageConstant.CART_PREPARE_ORDER_SUCCESS));
        } catch (Exception e) {
            log.error(CM061000MessageConstant.CART_DB_ERROR, e);
            return ResponseEntity.internalServerError()
                    .body(setResponseDto(null, false, CM061000MessageConstant.CART_PREPARE_ORDER_FAIL));
        }
    }

    /**
     * 現在認証されているユーザーIDを取得
     *
     * @return ユーザーID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("認証情報がありません。");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            Integer id = user.getUserId();
            if (id == null) {
                throw new AccessDeniedException("有効なユーザー情報がありません。");
            }
            return id.longValue();
        }
        throw new AccessDeniedException("有効なユーザー情報がありません。");
    }

    private String extractErrorMessage(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("\r\n "));
    }

    /**
     * カート追加リクエストボディ
     */
    @Data
    public static class AddCartRequest {

        @NotBlank(message = "productId は必須です。")
        private String productId;

        @NotNull(message = "quantity は必須です。")
        @Min(value = 1, message = "quantity は 1 以上である必要があります。")
        private Integer quantity;
    }

    /**
     * 数量変更リクエストボディ
     */
    @Data
    public static class UpdateQtyRequest {

        @NotNull(message = "quantity は必須です。")
        @Min(value = 1, message = "quantity は 1 以上である必要があります。")
        private Integer quantity;
    }

    /**
     * 注文準備リクエストボディ
     */
    @Data
    public static class PrepareOrderRequest {

        @NotEmpty(message = "cartItemIds は 1 件以上である必要があります。")
        private List<Long> cartItemIds;
    }

    /**
     * 選択削除リクエストボディ
     */
    @Data
    public static class DeleteSelectedCartRequest {

        @NotEmpty(message = "cartItemIds は 1 件以上である必要があります。")
        private List<Long> cartItemIds;
    }
    
    /**
     * 共通レスポンスモデル生成
     *
     * @param resultList 結果データ
     * @param ret        処理結果
     * @param msg        レスポンスメッセージ
     * @return ResponseModel オブジェクト
     */
    private <T> ResponseModel<T> setResponseDto(T resultList, boolean ret, String msg) {
        return ResponseModel.<T>builder()
                .resultList(resultList)
                .result(ret)
                .message(msg)
                .build();
    }
}
