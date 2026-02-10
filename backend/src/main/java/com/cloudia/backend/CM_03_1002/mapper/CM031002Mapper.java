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
     * カート関連一覧取得
     *
     * @return カート関連の全件一覧
     */
    List<CartItem> findActiveCartItemsByUserId(Long userId);
    CartItem findCartItem(@Param("userId") Long userId, @Param("productId") String productId);
    void updateQuantity(@Param("userId") Long userId, @Param("productId") String productId, @Param("quantity") int quantity);
    void insertCartItem(@Param("userId") Long userId, @Param("productId") String productId, @Param("quantity") int quantity);

    ProductInfo selectProductDetail(@Param("productId") Long productId);

    /**
     * 全件一覧取得（reservationDeadline がない商品のみ）
     *
     * @param categories 選択されたカテゴリ一覧
     * @return 新商品一覧
     */
    List<ProductInfo> selectNewProductList(@Param("categories") List<String> categories);

    /**
     * カテゴリーグループ全件一覧取得
     */
    List<Categories> findAllCategoryGroupCode();

    /**
     * 選択されたカテゴリーグループ配下のカテゴリ情報を取得
     */
    List<CategoryDetails> findCategory(@Param("categoryGroupCodes") List<String> categoryGroupCode);
}
