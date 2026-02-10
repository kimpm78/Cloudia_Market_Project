package com.cloudia.backend.CM_06_1000.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

import java.util.List;

import com.cloudia.backend.CM_06_1000.model.CartItem;
import com.cloudia.backend.CM_06_1000.model.CartItemResponse;
import com.cloudia.backend.CM_06_1000.model.CartProductMeta;

@Mapper
public interface CM061000Mapper {

    /**
     * 指定商品の最新在庫行をロックします。
     * 同時のカート追加／数量変更による競合を防ぐための用途です。
     *
     * @param productId 標準商品コード
     * @return ロックした stock_id（存在しない場合は null）
     */
    Long lockLatestStockRow(@Param("productId") String productId);

    /**
     * （物理在庫 available_qty）-（TTL 内の有効カート予約合計）+（自分のカート数量）を計算し、
     * ユーザー基準の最大投入可能数量を返します。
     *
     * @param userId    ユーザーID
     * @param productId 標準商品コード
     * @return 最大投入可能数量（存在しない場合は 0）
     */
    Integer findMaxPurchasableQuantity(@Param("userId") Long userId, @Param("productId") String productId);

    /**
     * カート一覧取得（有効項目のみ）
     *
     * @param userId ユーザーID
     * @return カート＋商品情報の組み合わせDTOリスト
     */
    List<CartItemResponse> findCartByUser(@Param("userId") Long userId);

    /**
     * カート投入検証用：現在のカート商品メタ情報を取得
     *
     * @param userId ユーザーID
     * @return 商品メタ情報リスト
     */
    List<CartProductMeta> findActiveCartProductMetas(@Param("userId") Long userId);

    /**
     * カート投入検証用：対象商品のメタ情報を取得
     *
     * @param productId 標準商品コード
     * @return 商品メタ情報
     */
    CartProductMeta findProductCartMeta(@Param("productId") String productId);

    /**
     * 同一商品がカートに有効状態で存在するかを確認
     *
     * @param userId    ユーザーID
     * @param productId 商品ID
     * @return 存在する場合は CartItem、存在しない場合は null
     */
    CartItem findActiveCartItem(@Param("userId") Long userId, @Param("productId") String productId);

    /**
     * カートに新規追加
     *
     * @param item カートエンティティ
     * @return insert count
     */
    int insertCartItem(CartItem item);

    /**
     * カート予約数量を増加
     */
    int increaseCartReservation(@Param("productId") String productId, @Param("quantity") int quantity);

    /**
     * カート予約数量を減少
     */
    int decreaseCartReservation(@Param("productId") String productId, @Param("quantity") int quantity);

    /**
     * 数量加算（同一商品が存在する場合）
     *
     * @param cartItemId カート項目ID
     * @param addQty     追加数量
     * @return update count
     */
    int increaseQuantity(@Param("cartItemId") Long cartItemId, @Param("addQty") int addQty,
        @Param("updatedBy") String updatedBy);

    /**
     * 数量を指定して変更
     *
     * @param cartItemId カート項目ID
     * @param quantity   変更数量（絶対値）
     * @return update count
     */
    int updateQuantity(@Param("cartItemId") Long cartItemId, @Param("quantity") int quantity,
        @Param("updatedBy") String updatedBy);

    /**
     * ソフト削除（論理削除）
     *
     * @param cartItemId カート項目ID
     * @param updatedBy  更新者
     * @return 削除（無効化）したレコード数
     */
    int softDelete(@Param("cartItemId") Long cartItemId, @Param("updatedBy") String updatedBy);

    /**
     * 選択したカート項目をソフト削除
     *
     * @param userId      ユーザーID
     * @param cartItemIds 削除するカート項目IDリスト
     * @param updatedBy   更新者
     * @return 削除（無効化）したレコード数
     */
    int softDeleteSelected(
            @Param("userId") Long userId,
            @Param("cartItemIds") List<Long> cartItemIds,
            @Param("updatedBy") String updatedBy);

    /**
     * cart_item_id でカート項目を取得
     *
     * @param cartItemId カート項目ID
     * @return CartItem
     */
    CartItem findCartItemById(@Param("cartItemId") Long cartItemId);

    /**
     * ユーザーの有効カート項目を取得
     */
    List<CartItem> findActiveCartItemsByUser(@Param("userId") Long userId);

    /**
     * 選択したカート項目を取得
     */
    List<CartItem> findActiveCartItemsByIds(@Param("userId") Long userId, @Param("cartItemIds") List<Long> cartItemIds);

    /**
     * 対象ユーザーのカートにおける最終更新日時
     *
     * @param userId ユーザーID
     * @return 最終 cart_updated_at
     */
    LocalDateTime findLastUpdatedAt(@Param("userId") Long userId);

    /**
     * 対象ユーザーのカートにおける最初の作成日時（TTL 基準）
     *
     * @param userId ユーザーID
     * @return 最初の created_at（存在しない場合は null）
     */
    LocalDateTime findCartCreatedAt(@Param("userId") Long userId);

    /**
     * カートの期限切れ処理（全項目を削除）
     *
     * @param userId    ユーザーID
     * @param updatedBy 更新者
     * @return delete count
     */
    int expireCartByUser(@Param("userId") Long userId, @Param("updatedBy") String updatedBy);

    /**
     * 選択項目の注文準備用取得
     *
     * @param userId      ユーザーID
     * @param cartItemIds 選択したカート項目IDリスト
     * @return 決済／注文準備用DTOリスト
     */
    List<CartItemResponse> findSelectedForOrder(@Param("userId") Long userId, @Param("cartItemIds") List<Long> cartItemIds);

    /**
     * 入力された識別子を標準商品コードに変換します。
     *
     * @param productId 商品ID または 商品コード
     * @return 標準商品コード または null
     */
    String findCanonicalProductId(@Param("productId") String productId);
}
