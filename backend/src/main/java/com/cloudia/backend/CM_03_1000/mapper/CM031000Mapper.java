package com.cloudia.backend.CM_03_1000.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_03_1000.model.CartItem;
import com.cloudia.backend.CM_03_1000.model.Categories;
import com.cloudia.backend.CM_03_1000.model.CategoryDetails;
import com.cloudia.backend.CM_03_1000.model.ProductInfo;

import java.util.List;

@Mapper
public interface CM031000Mapper {

    /**
     * 장바구니 관련 리스트 조회
     * 
     * @return 장바구니 관련 전체 리스트
     */
    List<CartItem> findActiveCartItemsByUserId(Long userId);
    CartItem findCartItem(@Param("userId") Long userId, @Param("productId") String productId);
    void updateQuantity(@Param("userId") Long userId,
        @Param("productId") String productId,
        @Param("quantity") int quantity,
        @Param("updatedBy") String updatedBy);
    void insertCartItem(@Param("userId") Long userId,
        @Param("productId") String productId,
        @Param("quantity") int quantity,
        @Param("createdBy") String createdBy,
        @Param("updatedBy") String updatedBy);

    /**
     * 상품 관련 리스트 조회 (선택된 카테고리 필터 포함)
     *
     * @param categories 선택된 카테고리 리스트
     * @return 필터링된 상품 리스트
     */
    List<ProductInfo> selectNewProductList(@Param("categories") List<String> categories);
    ProductInfo selectProductDetail(@Param("productId") Long productId);
    List<String> selectProductDetailImages(@Param("productId") Long productId);
    
    /**
     * 카테고리 그룹 코드 전체 리스트 조회
     * 
     * @return 카테고리 그룹 코드 전체 리스트
     */
    List<Categories> findAllCategoryGroupCode();

    /**
     * 선택 된 카테고리 그룹의 하위 카테고리 정보 조회
     * 
     * @param categoryGroupCode 카테고리 그룹 코드드
     * @return 하위 카테고리 정보
     */
    List<CategoryDetails> findCategory(@Param("categoryGroupCodes") List<String> categoryGroupCode);
}
