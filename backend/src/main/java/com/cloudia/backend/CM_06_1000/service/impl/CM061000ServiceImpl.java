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
     * カート一覧取得
     * @param userId ユーザーID
     * @return カートアイテム一覧
     */
    @Override
    @Transactional
    public List<CartItemResponse> getCart(Long userId) {
        try {
            if (userId == null) {
                LogHelper.log(LogMessage.VALIDATION_INPUT_INVALID, new String[] { "カート取得" });
                throw new IllegalArgumentException(ErrorCode.INVALID_INPUT_VALUE.getMessage());
            }
            expireCartIfNecessary(userId);
            List<CartItemResponse> list = cm061000Mapper.findCartByUser(userId);
            log.info(CM061000MessageConstant.CART_FETCH_SUCCESS);
            return list;
        } catch (Exception e) {
            LogHelper.log(LogMessage.DB_ACCESS_ERROR, new String[] { "カート取得" }, e);
            throw e;
        }
    }

    /**
     * カート最終更新日時取得
     * @param userId ユーザーID
     * @return 最終 cart_updated_at（存在しない場合は null）
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
     * カート初回作成日時取得（TTL基準）
     * @param userId ユーザーID
     * @return 初回 created_at（存在しない場合は null）
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
     * カート追加時の予約/通常商品の混在ルール検証
     *
     * @param userId ユーザーID
     * @param productId 商品IDまたはコード
     */
    @Override
    @Transactional(readOnly = true)
    public void validateCartMixOnAdd(Long userId, String productId) {
        if (userId == null) {
            LogHelper.log(LogMessage.VALIDATION_INPUT_INVALID, new String[] { "カート追加" });
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
     * カート追加
     * @param userId    ユーザーID
     * @param productId 商品ID
     * @param quantity  数量（1以上）
     * @return 追加または増加されたカートアイテムID
     */
    @Override
    @Transactional
    public Long addToCart(Long userId, String productId, int quantity) {
        try {
            // 入力値検証（fail fast）
            if (quantity < 1) {
                throw new IllegalArgumentException(CM061000MessageConstant.CART_INVALID_QUANTITY);
            }
            if (productId == null || productId.isBlank()) {
                throw new IllegalArgumentException(CM061000MessageConstant.CART_ITEM_NOT_FOUND);
            }
            // カート期限切れ処理
            expireCartIfNecessary(userId);
            // 商品コード正規化
            String normalizedProductId = normalizeProductId(productId);
            if (normalizedProductId == null) {
                throw new IllegalArgumentException(CM061000MessageConstant.CART_ITEM_NOT_FOUND);
            }

            ensureReleaseMonthCompatible(userId, normalizedProductId);

            cm061000Mapper.lockLatestStockRow(normalizedProductId);

            // 既存カートアイテム取得
            CartItem existingItem = cm061000Mapper.findActiveCartItem(userId, normalizedProductId);
            String updater = resolveUpdater();

            if (existingItem != null) {
                int desiredQuantity = existingItem.getQuantity() + quantity;
                ensurePurchaseLimit(normalizedProductId, desiredQuantity);
                ensureStockAvailable(userId, normalizedProductId, desiredQuantity);
                ensureCartLimit(userId, existingItem.getQuantity(), desiredQuantity);

                applyCartStockDelta(normalizedProductId, quantity);
                cm061000Mapper.increaseQuantity(existingItem.getCartItemId(), quantity, updater);
                log.info("カート数量増加 - userId: {}, productId: {}, cartItemId: {}, addQty: {}",
                        userId, normalizedProductId, existingItem.getCartItemId(), quantity);
                return existingItem.getCartItemId();
            }
            // 新規アイテム追加処理
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
            log.info("カート新規追加 - userId: {}, productId: {}, cartItemId: {}, quantity: {}",
                    userId, normalizedProductId, item.getCartItemId(), quantity);
            return item.getCartItemId();

        } catch (IllegalStateException | IllegalArgumentException e) {
            // ビジネス／検証エラーは WARN レベル + メッセージ
            log.warn("カート追加失敗 - userId: {}, productId: {}, reason: {}",
                    userId, productId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // システム／DBエラーは ERROR レベル
            LogHelper.log(LogMessage.DB_ACCESS_ERROR, new String[] { "カート追加" }, e);
            throw e;
        }
    }

    /**
     * 数量変更（絶対値指定）
     * @param userId     ユーザーID
     * @param cartItemId カートアイテムID
     * @param quantity   変更数量（1以上）
     */
    @Override
    @Transactional
    public void updateQuantity(Long userId, Long cartItemId, int quantity) {
        try {
            // 入力値検証（fail fast）
            if (quantity < 1) {
                throw new IllegalArgumentException(CM061000MessageConstant.CART_INVALID_QUANTITY);
            }

            // 先にカート期限切れ処理
            expireCartIfNecessary(userId);

            // 対象カートアイテム取得
            CartItem cartItem = cm061000Mapper.findCartItemById(cartItemId);
            if (cartItem == null || cartItem.getIsActive() == null || cartItem.getIsActive() == 0) {
                throw new IllegalArgumentException(CM061000MessageConstant.CART_ITEM_NOT_FOUND);
            }

            // 所有者検証（他ユーザーのカートを遮断）
            if (!userId.equals(cartItem.getUserId())) {
                // セキュリティ上「存在しない」として応答（存在有無の露出防止）
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
            // 数量更新
            cm061000Mapper.updateQuantity(cartItemId, quantity, resolveUpdater());

            log.info(
                    "カート数量変更成功 - userId: {}, cartItemId: {}, productId: {}, from: {}, to: {}",
                    userId, cartItemId, cartItem.getProductId(), cartItem.getQuantity(), quantity);

        } catch (IllegalStateException | IllegalArgumentException e) {
            // ビジネス／検証エラー
            log.warn(
                    "カート数量変更失敗 - userId: {}, cartItemId: {}, reason: {}",
                    userId, cartItemId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // システム／DBエラー
            log.error(CM061000MessageConstant.CART_DB_ERROR, e);
            throw e;
        }
    }

    /**
     * カートアイテム削除（ソフトデリート）
     *
     * @param userId     ユーザーID
     * @param cartItemId カートアイテムID
     */
    @Override
    @Transactional
    public void remove(Long userId, Long cartItemId) {
        try {
            // 先にカート期限切れ処理
            expireCartIfNecessary(userId);
            // 対象カートアイテム取得
            CartItem cartItem = cm061000Mapper.findCartItemById(cartItemId);
            if (cartItem == null || cartItem.getIsActive() == null || cartItem.getIsActive() == 0) {
                throw new IllegalArgumentException(CM061000MessageConstant.CART_ITEM_NOT_FOUND);
            }
            // 所有者検証（他ユーザーのカートを削除できないように）
            if (!userId.equals(cartItem.getUserId())) {
                // 存在有無を隠すため NOT_FOUND をそのまま使用
                throw new IllegalStateException(CM061000MessageConstant.CART_ITEM_NOT_FOUND);
            }
            applyCartStockDelta(cartItem.getProductId(), -1 * cartItem.getQuantity());
            // ソフトデリート
            cm061000Mapper.softDelete(cartItemId, resolveUpdater());
            log.info(
                    "カートアイテム削除成功 - userId: {}, cartItemId: {}, productId: {}",
                    userId, cartItemId, cartItem.getProductId());
        } catch (IllegalStateException | IllegalArgumentException e) {
            // ビジネス／検証エラー
            log.warn(
                    "カートアイテム削除失敗 - userId: {}, cartItemId: {}, reason: {}",
                    userId, cartItemId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // システム／DBエラー
            log.error(CM061000MessageConstant.CART_DB_ERROR, e);
            throw e;
        }
    }

    /**
     * カート全件削除（ソフトデリート）
     * @param userId ユーザーID
     */
    @Override
    @Transactional
    public void clearCart(Long userId) {
        try {
            List<CartItem> items = cm061000Mapper.findActiveCartItemsByUser(userId);
            releaseCartReservations(items);
            cm061000Mapper.expireCartByUser(userId, resolveUpdater());
            log.info("カート全件削除 - userId: {}", userId);
        } catch (Exception e) {
            log.error(CM061000MessageConstant.CART_DB_ERROR, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 選択したカートアイテム削除（ソフトデリート）
     * @param userId      ユーザーID
     * @param cartItemIds 削除するカートアイテムID一覧
     */
    @Override
    @Transactional
    public void deleteSelected(Long userId, List<Long> cartItemIds) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("userId は必須です。");
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
                    "選択したカートアイテム削除成功 - userId: {}, cartItemIds: {}, count: {}",
                    userId, cartItemIds, affected);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn(
                    "選択したカートアイテム削除失敗 - userId: {}, cartItemIds: {}, reason: {}",
                    userId, cartItemIds, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(CM061000MessageConstant.CART_DB_ERROR, e);
            throw e;
        }
    }

    /**
     * 注文準備 - 選択したカートアイテム取得
     * @param userId      ユーザーID
     * @param cartItemIds カートアイテムID一覧
     * @return 選択したカートアイテム一覧
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
     * （在庫 available_qty）-（TTL内の有効カート予約合計）+（自分の予約数量）を基準に検証
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
                    "在庫不足（予約反映）- productId: {}, desired: {}, maxAllowed: {}",
                    normalizedProductId, desiredQuantity, resolvedMax);
            throw new IllegalStateException(CM061000MessageConstant.CART_OUT_OF_STOCK);
        }
    }

    /**
     * 商品IDを正規化
     * @param rawProductId 元の商品コード
     * @return 正規化されたコード（無効な場合は null）
     */
    private String normalizeProductId(String rawProductId) {
        if (rawProductId == null) {
            return null;
        }
        String trimmed = rawProductId.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        // DB参照エラーは上位で処理するため、例外はそのまま伝播
        String canonical = cm061000Mapper.findCanonicalProductId(trimmed);
        if (canonical != null && !canonical.isBlank()) {
            return canonical.trim();
        }

        // 正規化情報がない場合は trim 済みの原本を使用
        return trimmed;
    }

    /**
     * 最終更新日時を基準にカートを期限切れ処理する。
     * @param userId ユーザーID
     */
    private void expireCartIfNecessary(Long userId) {
        if (userId == null) {
            return;
        }

        // 有効カートの「初回作成日時」を基準にTTL判定
        LocalDateTime createdAt = cm061000Mapper.findCartCreatedAt(userId);
        if (createdAt == null) {
            // 有効カートがなければ終了
            return;
        }

        LocalDateTime threshold =
                LocalDateTime.now().minus(CART_EXPIRE_MINUTES, ChronoUnit.MINUTES);

        if (createdAt.isBefore(threshold)) {
            List<CartItem> items = cm061000Mapper.findActiveCartItemsByUser(userId);
            releaseCartReservations(items);
            cm061000Mapper.expireCartByUser(userId, resolveUpdater());
            log.info(
                    "カート期限切れ処理 - userId: {}, createdAt: {}, threshold: {}",
                    userId, createdAt, threshold);
        }
    }

    /**
     * カート数量上限の検証
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
     * カート在庫予約数量の増減を反映
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
     * カート予約数量の解放を反映
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
     * 商品別の最大購入数量制限の検証
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
     * カート追加時の予約/通常商品の混在ルール検証
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
     * 発売月文字列の正規化（YYYY-MM）
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
     * DBの createdBy / updatedBy に設定するユーザー識別子。
     * @return 現在認証されたユーザー名、または "SYSTEM"
     */
    private String resolveUpdater() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "SYSTEM";
    }
}
