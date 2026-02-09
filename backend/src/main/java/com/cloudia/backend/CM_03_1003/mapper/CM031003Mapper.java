package com.cloudia.backend.CM_03_1003.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_03_1003.model.CartItem;
import com.cloudia.backend.CM_03_1003.model.Categories;
import com.cloudia.backend.CM_03_1003.model.CategoryDetails;
import com.cloudia.backend.CM_03_1003.model.ProductInfo;

import java.util.List;

@Mapper
public interface CM031003Mapper {

    /**
     * カート関連の一覧取得
     *
     * @return カート関連の全リスト
     */
    List<CartItem> findActiveCartItemsByUserId(Long userId);

    CartItem findCartItem(@Param("userId") Long userId, @Param("productId") String productId);

    void updateQuantity(
        @Param("userId") Long userId,
        @Param("productId") String productId,
        @Param("quantity") int quantity
    );

    void insertCartItem(
        @Param("userId") Long userId,
        @Param("productId") String productId,
        @Param("quantity") int quantity
    );

    ProductInfo selectProductDetail(@Param("productId") Long productId);

    /**
     * 一覧取得（reservationDeadline がない商品のみ）
     *
     * @param categories 選択されたカテゴリ一覧
     * @return 新商品一覧
     */
    List<ProductInfo> selectNewProductList(@Param("categories") List<String> categories);

    /**
     * 全カテゴリグループ一覧取得
     */
    List<Categories> findAllCategoryGroupCode();

    /**
     * 選択されたカテゴリグループの下位カテゴリ情報取得
     */
    List<CategoryDetails> findCategory(@Param("categoryGroupCodes") List<String> categoryGroupCode);

}
