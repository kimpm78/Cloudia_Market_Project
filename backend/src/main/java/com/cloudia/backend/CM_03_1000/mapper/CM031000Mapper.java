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
     * カート関連一覧取得
     * 
     * @return カート関連一覧
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
     * 商品一覧取得（選択カテゴリのフィルタ含む）
     *
     * @param categories 選択されたカテゴリ一覧
     * @return フィルタ後の商品一覧
     */
    List<ProductInfo> selectNewProductList(@Param("categories") List<String> categories);
    ProductInfo selectProductDetail(@Param("productId") Long productId);
    List<String> selectProductDetailImages(@Param("productId") Long productId);
    
    /**
     * カテゴリグループコード一覧取得
     * 
     * @return カテゴリグループコード一覧
     */
    List<Categories> findAllCategoryGroupCode();

    /**
     * 選択したカテゴリグループの下位カテゴリ情報取得
     * 
     * @param categoryGroupCode カテゴリグループコード
     * @return 下位カテゴリ情報
     */
    List<CategoryDetails> findCategory(@Param("categoryGroupCodes") List<String> categoryGroupCode);
}
