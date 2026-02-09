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
     * 특정 상품의 최신 재고 행을 잠금 처리한다.
     * 동시 장바구니 담기/수량변경 경쟁조건을 막기 위한 용도.
     *
     * @param productId 표준 상품 코드(=cart_items.product_id)
     * @return 잠금된 stock_id (없으면 null)
     */
    Long lockLatestStockRow(@Param("productId") String productId);

    /**
     * (물리 재고 available_qty) - (TTL 내 활성 장바구니 예약 합계) + (내 장바구니 수량)을 계산한, 해당 사용자 기준 최대 담기 가능 수량을 반환한다.
     *
     * @param userId 사용자 ID
     * @param productId 표준 상품 코드(=cart_items.product_id)
     * @return 최대 담기 가능 수량(없으면 0)
     */
    Integer findMaxPurchasableQuantity(@Param("userId") Long userId, @Param("productId") String productId);

    /**
     * 장바구니 목록 조회 (유효 항목만)
     *
     * @param userId 사용자 ID
     * @return 장바구니 + 상품정보 조합 DTO 리스트
     */
    List<CartItemResponse> findCartByUser(@Param("userId") Long userId);

    /**
     * 장바구니 담기 검증용: 현재 장바구니 상품 메타 조회
     *
     * @param userId 사용자 ID
     * @return 상품 메타 리스트
     */
    List<CartProductMeta> findActiveCartProductMetas(@Param("userId") Long userId);

    /**
     * 장바구니 담기 검증용: 대상 상품 메타 조회
     *
     * @param productId 표준 상품 코드
     * @return 상품 메타
     */
    CartProductMeta findProductCartMeta(@Param("productId") String productId);

    /**
     * 동일 상품이 장바구니에 활성 상태로 존재하는지 확인
     *
     * @param userId    사용자 ID
     * @param productId 상품 ID
     * @return 존재 시 CartItem, 없으면 null
     */
    CartItem findActiveCartItem(@Param("userId") Long userId, @Param("productId") String productId);

    /**
     * 장바구니 신규 추가
     *
     * @param item 장바구니 엔티티
     * @return insert count
     */
    int insertCartItem(CartItem item);

    /**
     * 장바구니 예약 수량 증가
     */
    int increaseCartReservation(@Param("productId") String productId, @Param("quantity") int quantity);

    /**
     * 장바구니 예약 수량 감소
     */
    int decreaseCartReservation(@Param("productId") String productId, @Param("quantity") int quantity);

    /**
     * 수량 가산 (동일 상품 존재 시)
     *
     * @param cartItemId 장바구니 항목 ID
     * @param addQty     추가 수량
     * @return update count
     */
    int increaseQuantity(@Param("cartItemId") Long cartItemId, @Param("addQty") int addQty,
        @Param("updatedBy") String updatedBy);

    /**
     * 수량 지정 변경
     *
     * @param cartItemId 장바구니 항목 ID
     * @param quantity   변경 수량(절대값)
     * @return update count
     */
    int updateQuantity(@Param("cartItemId") Long cartItemId, @Param("quantity") int quantity,
        @Param("updatedBy") String updatedBy);

    /**
     * 소프트 삭제(논리 삭제)
     * @param cartItemId 장바구니 항목 ID
     * @param updatedBy  수정자
     * @return 삭제(비활성 처리)된 레코드 수
     */
    int softDelete(@Param("cartItemId") Long cartItemId, @Param("updatedBy") String updatedBy);

    /**
     * 선택된 장바구니 항목 소프트 삭제
     * @param userId      사용자 ID
     * @param cartItemIds 삭제할 장바구니 항목 ID 리스트
     * @param updatedBy   수정자
     * @return 삭제(비활성 처리)된 레코드 수
     */
    int softDeleteSelected(
            @Param("userId") Long userId,
            @Param("cartItemIds") List<Long> cartItemIds,
            @Param("updatedBy") String updatedBy);

    /**
     * cart_item_id로 장바구니 항목 조회
     *
     * @param cartItemId 장바구니 항목 ID
     * @return CartItem
     */
    CartItem findCartItemById(@Param("cartItemId") Long cartItemId);

    /**
     * 사용자 활성 장바구니 항목 조회
     */
    List<CartItem> findActiveCartItemsByUser(@Param("userId") Long userId);

    /**
     * 선택된 장바구니 항목 조회
     */
    List<CartItem> findActiveCartItemsByIds(@Param("userId") Long userId, @Param("cartItemIds") List<Long> cartItemIds);

    /**
     * 해당 사용자의 장바구니 중 가장 최근 업데이트 시각
     *
     * @param userId 사용자 ID
     * @return 마지막 cart_updated_at
     */
    LocalDateTime findLastUpdatedAt(@Param("userId") Long userId);

    /**
     * 해당 사용자의 장바구니 중 최초 생성 시각(TTL 기준)
     * @param userId 사용자 ID
     * @return 최초 created_at (없으면 null)
     */
    LocalDateTime findCartCreatedAt(@Param("userId") Long userId);

    /**
     * 장바구니 만료 처리 (모든 항목 삭제)
     *
     * @param userId 사용자 ID
     * @param updatedBy 수정자
     * @return delete count
     */
    int expireCartByUser(@Param("userId") Long userId, @Param("updatedBy") String updatedBy);

    /**
     * 선택 항목 주문 준비용 조회
     *
     * @param userId      사용자 ID
     * @param cartItemIds 선택한 장바구니 항목 ID 리스트
     * @return 결제/주문 준비용 DTO 리스트
     */
    List<CartItemResponse> findSelectedForOrder(@Param("userId") Long userId, @Param("cartItemIds") List<Long> cartItemIds);

    /**
     * 입력된 식별자를 표준 상품 코드로 변환합니다.
     *
     * @param productId 상품 ID 또는 상품 코드
     * @return 표준 상품 코드 또는 null
     */
    String findCanonicalProductId(@Param("productId") String productId);
}
