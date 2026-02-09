package com.cloudia.backend.CM_03_1002.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_03_1002.model.CartItem;
import com.cloudia.backend.CM_03_1002.model.Categories;
import com.cloudia.backend.CM_03_1002.model.CategoryDetails;
import com.cloudia.backend.CM_03_1002.model.ProductInfo;

import java.util.List;

@Mapper
public interface CM031002Mapper {

    /**
     * 장바구니 관련 리스트 조회
     * 
     * @return 장바구니 관련 전체 리스트
     */
    List<CartItem> findActiveCartItemsByUserId(Long userId);
    CartItem findCartItem(@Param("userId") Long userId, @Param("productId") String productId);
    void updateQuantity(@Param("userId") Long userId, @Param("productId") String productId, @Param("quantity") int quantity);
    void insertCartItem(@Param("userId") Long userId, @Param("productId") String productId, @Param("quantity") int quantity);

    ProductInfo selectProductDetail(@Param("productId") Long productId);

    /**
     * 전체 리스트 조회 (reservationDeadline이 없는 상품만)
     *
     * @param categories 선택된 카테고리 리스트
     * @return 신상품 리스트
     */
    List<ProductInfo> selectNewProductList(@Param("categories") List<String> categories);

    /**
     * 전체 카테고리 그룹 리스트 조회
     */
    List<Categories> findAllCategoryGroupCode();

    /**
     * 선택된 카테고리 그룹의 하위 카테고리 정보 조회
     */
    List<CategoryDetails> findCategory(@Param("categoryGroupCodes") List<String> categoryGroupCode);
}
