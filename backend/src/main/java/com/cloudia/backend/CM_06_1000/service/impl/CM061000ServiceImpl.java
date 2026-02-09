package com.cloudia.backend.CM_06_1000.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_06_1000.constants.CM061000MessageConstant;
import com.cloudia.backend.CM_06_1000.mapper.CM061000Mapper;
import com.cloudia.backend.CM_06_1000.model.CartItem;
import com.cloudia.backend.CM_06_1000.model.CartItemResponse;
import com.cloudia.backend.CM_06_1000.model.CartProductMeta;
import com.cloudia.backend.CM_06_1000.service.CM061000Service;
import com.cloudia.backend.common.exception.ErrorCode;
import com.cloudia.backend.common.log.LogHelper;
import com.cloudia.backend.common.log.LogMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM061000ServiceImpl implements CM061000Service {

    private static final long CART_EXPIRE_MINUTES = 30L;
    private static final int CART_MAX_QUANTITY = 10;

    private final CM061000Mapper cm061000Mapper;

    /**
     * 장바구니 목록 조회
     * @param userId 사용자 ID
     * @return 장바구니 항목 리스트
     */
    @Override
    @Transactional
    public List<CartItemResponse> getCart(Long userId) {
        try {
            if (userId == null) {
                LogHelper.log(LogMessage.VALIDATION_INPUT_INVALID, new String[] { "장바구니 조회" });
                throw new IllegalArgumentException(ErrorCode.INVALID_INPUT_VALUE.getMessage());
            }
            expireCartIfNecessary(userId);
            List<CartItemResponse> list = cm061000Mapper.findCartByUser(userId);
            log.info(CM061000MessageConstant.CART_FETCH_SUCCESS);
            return list;
        } catch (Exception e) {
            LogHelper.log(LogMessage.DB_ACCESS_ERROR, new String[] { "장바구니 조회" }, e);
            throw e;
        }
    }

    /**
     * 장바구니 마지막 갱신 시각 조회
     * @param userId 사용자 ID
     * @return 마지막 cart_updated_at (없으면 null)
     */
    @Override
    public LocalDateTime getLastUpdatedAt(Long userId) {
        if (userId == null) {
            return null;
        }
        expireCartIfNecessary(userId);
        return cm061000Mapper.findLastUpdatedAt(userId);
    }

    /**
     * 장바구니 최초 생성 시각 조회 (TTL 기준)
     * @param userId 사용자 ID
     * @return 최초 created_at (없으면 null)
     */
    @Override
    public LocalDateTime getCreatedAt(Long userId) {
        if (userId == null) {
            return null;
        }
        expireCartIfNecessary(userId);
        return cm061000Mapper.findCartCreatedAt(userId);
    }

    /**
     * 장바구니 담기 시 예약/상시 혼합 규칙 검증
     *
     * @param userId 사용자 ID
     * @param productId 상품 ID 또는 코드
     */
    @Override
    @Transactional(readOnly = true)
    public void validateCartMixOnAdd(Long userId, String productId) {
        if (userId == null) {
            LogHelper.log(LogMessage.VALIDATION_INPUT_INVALID, new String[] { "장바구니 담기" });
            throw new IllegalArgumentException(ErrorCode.INVALID_INPUT_VALUE.getMessage());
        }
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException(CM061000MessageConstant.CART_ITEM_NOT_FOUND);
        }
        String normalized = normalizeProductId(productId);
        if (normalized == null) {
            throw new IllegalArgumentException(CM061000MessageConstant.CART_ITEM_NOT_FOUND);
        }
        ensureReleaseMonthCompatible(userId, normalized);
    }

    /**
     * 장바구니 담기
     * @param userId    사용자 ID
     * @param productId 상품 ID
     * @param quantity  수량(1 이상)
     * @return 추가 또는 증가된 장바구니 항목 ID
     */
    @Override
    @Transactional
    public Long addToCart(Long userId, String productId, int quantity) {
        try {
            // 입력값 검증 - fail fast
            if (quantity < 1) {
                throw new IllegalArgumentException(CM061000MessageConstant.CART_INVALID_QUANTITY);
            }
            if (productId == null || productId.isBlank()) {
                throw new IllegalArgumentException(CM061000MessageConstant.CART_ITEM_NOT_FOUND);
            }
            // 장바구니 만료 처리
            expireCartIfNecessary(userId);
            // 상품코드 정규화
            String normalizedProductId = normalizeProductId(productId);
            if (normalizedProductId == null) {
                throw new IllegalArgumentException(CM061000MessageConstant.CART_ITEM_NOT_FOUND);
            }

            ensureReleaseMonthCompatible(userId, normalizedProductId);

            // 상품 단위 경쟁조건 방지: 재고 행 잠금 후 남은 수량 계산
            cm061000Mapper.lockLatestStockRow(normalizedProductId);

            // 기존 장바구니 항목 조회
            CartItem existingItem = cm061000Mapper.findActiveCartItem(userId, normalizedProductId);
            String updater = resolveUpdater();

            if (existingItem != null) {
                int desiredQuantity = existingItem.getQuantity() + quantity;
                // 재고 / 제한 검증
                ensurePurchaseLimit(normalizedProductId, desiredQuantity);
                ensureStockAvailable(userId, normalizedProductId, desiredQuantity);
                ensureCartLimit(userId, existingItem.getQuantity(), desiredQuantity);

                applyCartStockDelta(normalizedProductId, quantity);
                cm061000Mapper.increaseQuantity(existingItem.getCartItemId(), quantity, updater);
                log.info("장바구니 수량 증가 - userId: {}, productId: {}, cartItemId: {}, addQty: {}",
                        userId, normalizedProductId, existingItem.getCartItemId(), quantity);
                return existingItem.getCartItemId();
            }
            // 새 항목 추가 처리
            ensurePurchaseLimit(normalizedProductId, quantity);
            ensureStockAvailable(userId, normalizedProductId, quantity);
            ensureCartLimit(userId, 0, quantity);

            applyCartStockDelta(normalizedProductId, quantity);
            CartItem item = CartItem.builder()
                    .userId(userId)
                    .productId(normalizedProductId)
                    .quantity(quantity)
                    .createdBy(updater)
                    .updatedBy(updater)
                    .build();
            cm061000Mapper.insertCartItem(item);
            log.info("장바구니 신규 추가 - userId: {}, productId: {}, cartItemId: {}, quantity: {}",
                    userId, normalizedProductId, item.getCartItemId(), quantity);
            return item.getCartItemId();

        } catch (IllegalStateException | IllegalArgumentException e) {
            // 비즈니스/검증 오류는 WARN 레벨 + 메시지
            log.warn("장바구니 담기 실패 - userId: {}, productId: {}, reason: {}",
                    userId, productId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 시스템/DB 오류는 ERROR 레벨
            LogHelper.log(LogMessage.DB_ACCESS_ERROR, new String[] { "장바구니 담기" }, e);
            throw e;
        }
    }

    /**
     * 수량 변경 (절대값 지정)
     * @param userId     사용자 ID
     * @param cartItemId 장바구니 항목 ID
     * @param quantity   변경 수량(1 이상)
     */
    @Override
    @Transactional
    public void updateQuantity(Long userId, Long cartItemId, int quantity) {
        try {
            // 입력값 검증 - fail fast
            if (quantity < 1) {
                throw new IllegalArgumentException(CM061000MessageConstant.CART_INVALID_QUANTITY);
            }

            // 장바구니 만료 처리 먼저
            expireCartIfNecessary(userId);

            // 대상 장바구니 항목 조회
            CartItem cartItem = cm061000Mapper.findCartItemById(cartItemId);
            if (cartItem == null || cartItem.getIsActive() == null || cartItem.getIsActive() == 0) {
                throw new IllegalArgumentException(CM061000MessageConstant.CART_ITEM_NOT_FOUND);
            }

            // 소유자 검증 (다른 사람 카트 차단)
            if (!userId.equals(cartItem.getUserId())) {
                // 보안상 "없다"로 응답 (존재 여부 노출 방지)
                throw new IllegalStateException(CM061000MessageConstant.CART_ITEM_NOT_FOUND);
            }

            // 재고 / 제한 검증
            cm061000Mapper.lockLatestStockRow(cartItem.getProductId());
            ensurePurchaseLimit(cartItem.getProductId(), quantity);
            ensureStockAvailable(userId, cartItem.getProductId(), quantity);
            ensureCartLimit(userId, cartItem.getQuantity(), quantity);

            int delta = quantity - (cartItem.getQuantity() != null ? cartItem.getQuantity() : 0);
            if (delta != 0) {
                applyCartStockDelta(cartItem.getProductId(), delta);
            }
            // 수량 업데이트
            cm061000Mapper.updateQuantity(cartItemId, quantity, resolveUpdater());

            log.info(
                    "장바구니 수량 변경 성공 - userId: {}, cartItemId: {}, productId: {}, from: {}, to: {}",
                    userId, cartItemId, cartItem.getProductId(), cartItem.getQuantity(), quantity);

        } catch (IllegalStateException | IllegalArgumentException e) {
            // 비즈니스/검증 오류
            log.warn(
                    "장바구니 수량 변경 실패 - userId: {}, cartItemId: {}, reason: {}",
                    userId, cartItemId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 시스템/DB 오류
            log.error(CM061000MessageConstant.CART_DB_ERROR, e);
            throw e;
        }
    }

    /**
     * 장바구니 항목 삭제 (소프트 딜리트).
     *
     * @param userId     사용자 ID
     * @param cartItemId 장바구니 항목 ID
     */
    @Override
    @Transactional
    public void remove(Long userId, Long cartItemId) {
        try {
            // 먼저 장바구니 만료 처리
            expireCartIfNecessary(userId);
            // 대상 장바구니 항목 조회
            CartItem cartItem = cm061000Mapper.findCartItemById(cartItemId);
            if (cartItem == null || cartItem.getIsActive() == null || cartItem.getIsActive() == 0) {
                throw new IllegalArgumentException(CM061000MessageConstant.CART_ITEM_NOT_FOUND);
            }
            // 소유자 검증 (다른 사람의 카트를 지우지 못하도록)
            if (!userId.equals(cartItem.getUserId())) {
                // 존재 여부를 숨기기 위해 NOT_FOUND 그대로 사용
                throw new IllegalStateException(CM061000MessageConstant.CART_ITEM_NOT_FOUND);
            }
            applyCartStockDelta(cartItem.getProductId(), -1 * cartItem.getQuantity());
            // 소프트 삭제
            cm061000Mapper.softDelete(cartItemId, resolveUpdater());
            log.info(
                    "장바구니 항목 삭제 성공 - userId: {}, cartItemId: {}, productId: {}",
                    userId, cartItemId, cartItem.getProductId());
        } catch (IllegalStateException | IllegalArgumentException e) {
            // 비즈니스/검증 에러
            log.warn(
                    "장바구니 항목 삭제 실패 - userId: {}, cartItemId: {}, reason: {}",
                    userId, cartItemId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 시스템/DB 에러
            log.error(CM061000MessageConstant.CART_DB_ERROR, e);
            throw e;
        }
    }

    /**
     * 장바구니 전체 삭제 (소프트 딜리트)
     * @param userId 사용자 ID
     */
    @Override
    @Transactional
    public void clearCart(Long userId) {
        try {
            List<CartItem> items = cm061000Mapper.findActiveCartItemsByUser(userId);
            releaseCartReservations(items);
            cm061000Mapper.expireCartByUser(userId, resolveUpdater());
            log.info("장바구니 전체 삭제 - userId: {}", userId);
        } catch (Exception e) {
            log.error(CM061000MessageConstant.CART_DB_ERROR, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 선택된 장바구니 항목 삭제 (소프트 딜리트)
     * @param userId      사용자 ID
     * @param cartItemIds 삭제할 장바구니 항목 ID 리스트
     */
    @Override
    @Transactional
    public void deleteSelected(Long userId, List<Long> cartItemIds) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("userId는 필수입니다.");
            }
            if (cartItemIds == null || cartItemIds.isEmpty()) {
                throw new IllegalArgumentException(CM061000MessageConstant.CART_PREPARE_ORDER_EMPTY);
            }
            List<CartItem> items = cm061000Mapper.findActiveCartItemsByIds(userId, cartItemIds);
            releaseCartReservations(items);
            int affected = cm061000Mapper.softDeleteSelected(userId, cartItemIds, resolveUpdater());
            if (affected == 0) {
                throw new IllegalStateException(CM061000MessageConstant.CART_ITEM_NOT_FOUND);
            }
            log.info(
                    "선택된 장바구니 항목 삭제 성공 - userId: {}, cartItemIds: {}, count: {}",
                    userId, cartItemIds, affected);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn(
                    "선택된 장바구니 항목 삭제 실패 - userId: {}, cartItemIds: {}, reason: {}",
                    userId, cartItemIds, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(CM061000MessageConstant.CART_DB_ERROR, e);
            throw e;
        }
    }

    /**
     * 주문 준비 - 선택된 장바구니 항목 조회
     * @param userId      사용자 ID
     * @param cartItemIds 장바구니 항목 ID 리스트
     * @return 선택된 장바구니 항목 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> prepareOrder(Long userId, List<Long> cartItemIds) {
        try {
            List<CartItemResponse> items = cm061000Mapper.findSelectedForOrder(userId, cartItemIds);
            log.info(CM061000MessageConstant.CART_PREPARE_ORDER_SUCCESS);
            return items;
        } catch (Exception e) {
            log.error(CM061000MessageConstant.CART_DB_ERROR, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * (재고 available_qty) - (TTL 내 활성 장바구니 예약 합계) + (내 예약 수량)   기준으로 검증
     */
    private void ensureStockAvailable(Long userId, String productId, int desiredQuantity) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException(CM061000MessageConstant.CART_ITEM_NOT_FOUND);
        }
        if (desiredQuantity < 1) {
            throw new IllegalArgumentException(CM061000MessageConstant.CART_INVALID_QUANTITY);
        }

        String normalizedProductId = productId.trim();
        Integer maxQty = cm061000Mapper.findMaxPurchasableQuantity(userId, normalizedProductId);
        int resolvedMax = maxQty != null ? maxQty : 0;

        if (resolvedMax <= 0 || desiredQuantity > resolvedMax) {
            log.warn(
                    "재고 부족(예약 반영) - productId: {}, desired: {}, maxAllowed: {}",
                    normalizedProductId, desiredQuantity, resolvedMax);
            throw new IllegalStateException(CM061000MessageConstant.CART_OUT_OF_STOCK);
        }
    }

    /**
     * 상품 ID를 정규화
     * @param rawProductId 원본 상품 코드
     * @return 정규화된 코드, 유효하지 않으면 null
     */
    private String normalizeProductId(String rawProductId) {
        if (rawProductId == null) {
            return null;
        }
        String trimmed = rawProductId.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        // DB 조회 오류는 상위에서 처리하도록 예외를 그대로 전파
        String canonical = cm061000Mapper.findCanonicalProductId(trimmed);
        if (canonical != null && !canonical.isBlank()) {
            return canonical.trim();
        }

        // 정규화 정보가 없으면 trim 된 원본 사용
        return trimmed;
    }

    /**
     * 마지막 갱신 시각을 기준으로 장바구니를 만료 처리한다.
     * @param userId 사용자 ID
     */
    private void expireCartIfNecessary(Long userId) {
        if (userId == null) {
            return;
        }

        // 활성 장바구니 중 "최초 생성 시각"을 기준으로 TTL 판단
        LocalDateTime createdAt = cm061000Mapper.findCartCreatedAt(userId);
        if (createdAt == null) {
            // 활성 장바구니가 없으면 종료
            return;
        }

        LocalDateTime threshold =
                LocalDateTime.now().minus(CART_EXPIRE_MINUTES, ChronoUnit.MINUTES);

        if (createdAt.isBefore(threshold)) {
            List<CartItem> items = cm061000Mapper.findActiveCartItemsByUser(userId);
            releaseCartReservations(items);
            cm061000Mapper.expireCartByUser(userId, resolveUpdater());
            log.info(
                    "장바구니 만료 처리 - userId: {}, createdAt: {}, threshold: {}",
                    userId, createdAt, threshold);
        }
    }

    /**
     * 장바구니 수량 제한 검증
     * @param userId
     * @param currentItemQuantity
     * @param desiredItemQuantity
     */
    private void ensureCartLimit(Long userId, int currentItemQuantity, int desiredItemQuantity) {
        if (desiredItemQuantity > CART_MAX_QUANTITY) {
            throw new IllegalStateException(CM061000MessageConstant.CART_LIMIT_EXCEEDED);
        }
    }

    /**
     * 장바구니 재고 예약 수량 증감 적용
     * 
     * @param productId
     * @param delta
     */
    private void applyCartStockDelta(String productId, int delta) {
        if (productId == null || productId.isBlank() || delta == 0) {
            return;
        }
        if (delta > 0) {
            int updated = cm061000Mapper.increaseCartReservation(productId, delta);
            if (updated == 0) {
                throw new IllegalStateException(CM061000MessageConstant.CART_OUT_OF_STOCK);
            }
        } else {
            cm061000Mapper.decreaseCartReservation(productId, Math.abs(delta));
        }
    }

    /** 
     * 장바구니 예약 수량 해제 적용
     * 
     * @param items
     */
    private void releaseCartReservations(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Map<String, Integer> totals = new HashMap<>();
        for (CartItem item : items) {
            if (item == null || item.getProductId() == null) {
                continue;
            }
            int qty = item.getQuantity() != null ? item.getQuantity() : 0;
            if (qty <= 0) {
                continue;
            }
            totals.merge(item.getProductId(), qty, Integer::sum);
        }
        totals.forEach((productId, qty) -> cm061000Mapper.decreaseCartReservation(productId, qty));
    }

    /**
     * 상품별 최대 구매 수량 제한 검증
     * @param productId
     * @param desiredItemQuantity
     */
    private void ensurePurchaseLimit(String productId, int desiredItemQuantity) {
        if (productId == null || productId.isBlank()) {
            return;
        }
        CartProductMeta meta = cm061000Mapper.findProductCartMeta(productId);
        Integer limit = meta != null ? meta.getPurchaseLimit() : null;
        int resolvedLimit = (limit == null || limit <= 0) ? 1 : limit;
        if (desiredItemQuantity > resolvedLimit) {
            throw new IllegalStateException(
                    String.format(CM061000MessageConstant.CART_PRODUCT_LIMIT_EXCEEDED, resolvedLimit));
        }
    }

    /**
     * 장바구니 담기 시 예약/상시 혼합 규칙 검증
     * 
     * @param userId
     * @param productId
     */
    private void ensureReleaseMonthCompatible(Long userId, String productId) {
        if (userId == null || productId == null || productId.isBlank()) {
            return;
        }

        CartProductMeta incoming = cm061000Mapper.findProductCartMeta(productId);
        if (incoming == null) {
            return;
        }

        boolean incomingReservation = Boolean.TRUE.equals(incoming.getReservation());
        String incomingMonth = normalizeReleaseMonth(incoming.getReleaseMonth());

        List<CartProductMeta> existing = cm061000Mapper.findActiveCartProductMetas(userId);
        if (existing == null || existing.isEmpty()) {
            return;
        }

        for (CartProductMeta item : existing) {
            if (item == null) {
                continue;
            }
            if (Objects.equals(productId, item.getProductId())) {
                continue;
            }

            boolean existingReservation = Boolean.TRUE.equals(item.getReservation());
            if (incomingReservation != existingReservation) {
                throw new IllegalStateException(
                        CM061000MessageConstant.CART_RESERVATION_MONTH_MISMATCH_ON_ADD);
            }

            if (!incomingReservation) {
                continue;
            }

            String existingMonth = normalizeReleaseMonth(item.getReleaseMonth());
            if (incomingMonth == null || existingMonth == null) {
                throw new IllegalStateException(
                        CM061000MessageConstant.CART_RESERVATION_MONTH_UNKNOWN_ON_ADD);
            }

            if (!Objects.equals(existingMonth, incomingMonth)) {
                throw new IllegalStateException(
                        CM061000MessageConstant.CART_RESERVATION_ONLY_MONTH_MISMATCH_ON_ADD);
            }
        }
    }

    /**
     * 출시월 문자열 정규화 (YYYY-MM)
     * 
     * @param source
     * @return
     */
    private String normalizeReleaseMonth(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        String normalized = source.trim();
        if (normalized.length() < 7) {
            return null;
        }
        return normalized.substring(0, 7);
    }

    /**
     * DB의 createdBy / updatedBy 에 설정할 사용자 식별자.
     * @return 현재 인증된 사용자 이름 또는 "SYSTEM"
     */
    private String resolveUpdater() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "SYSTEM";
    }
}
