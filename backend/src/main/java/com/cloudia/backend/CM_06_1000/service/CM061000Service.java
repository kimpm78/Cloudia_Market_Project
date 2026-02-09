package com.cloudia.backend.CM_06_1000.service;

import java.util.List;

import com.cloudia.backend.CM_06_1000.model.CartItemResponse;

public interface CM061000Service {

    /**
     * カート一覧取得
     *
     * @param userId ユーザーID
     * @return カート項目リスト
     */
    List<CartItemResponse> getCart(Long userId);

    /**
     * カートに追加（同一商品が存在する場合は数量を加算）
     *
     * @param userId    ユーザーID
     * @param productId 商品ID
     * @param quantity  数量（1以上）
     * @return cartItemId
     */
    Long addToCart(Long userId, String productId, int quantity);

    /**
     * カート数量変更（絶対値で設定）
     *
     * @param userId     ユーザーID
     * @param cartItemId カート項目ID
     * @param quantity   変更数量（1以上）
     */
    void updateQuantity(Long userId, Long cartItemId, int quantity);

    /**
     * カート項目削除（ソフトデリート）
     *
     * @param userId     ユーザーID
     * @param cartItemId カート項目ID
     */
    void remove(Long userId, Long cartItemId);

    /**
     * カート全件削除
     *
     * @param userId ユーザーID
     */
    void clearCart(Long userId);

    /**
     * 選択したカート項目を削除（ソフトデリート）
     *
     * @param userId      ユーザーID
     * @param cartItemIds 削除対象のカート項目IDリスト
     */
    void deleteSelected(Long userId, List<Long> cartItemIds);

    /**
     * 注文準備：選択したカート項目のみ取得
     *
     * @param userId      ユーザーID
     * @param cartItemIds 選択したカート項目IDリスト
     * @return 決済／注文準備用リスト
     */
    List<CartItemResponse> prepareOrder(Long userId, List<Long> cartItemIds);

    /**
     * カート最終更新日時の取得
     *
     * @param userId ユーザーID
     * @return 最終 cart_updated_at（存在しない場合は null）
     */
    java.time.LocalDateTime getLastUpdatedAt(Long userId);

    /**
     * カート初回作成日時の取得（TTL基準）
     *
     * @param userId ユーザーID
     * @return 初回 created_at（存在しない場合は null）
     */
    java.time.LocalDateTime getCreatedAt(Long userId);

    /**
     * カート追加時の「予約／通常」混在ルール検証
     *
     * @param userId    ユーザーID
     * @param productId 商品ID または 商品コード
     */
    void validateCartMixOnAdd(Long userId, String productId);
}
