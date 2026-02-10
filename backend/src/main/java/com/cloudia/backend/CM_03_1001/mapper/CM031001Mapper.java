package com.cloudia.backend.CM_03_1001.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_03_1001.model.CartItem;
import com.cloudia.backend.CM_03_1001.model.Categories;
import com.cloudia.backend.CM_03_1001.model.CategoryDetails;
import com.cloudia.backend.CM_03_1001.model.ProductDetails;
import com.cloudia.backend.CM_03_1001.model.ProductInfo;

import java.util.List;

@Mapper
public interface CM031001Mapper {

    /**
     * カート関連一覧取得
     *
     * @return カート関連の全件一覧
     */
    List<CartItem> findActiveCartItemsByUserId(Long userId);
    CartItem findCartItem(@Param("userId") Long userId, @Param("productId") String productId);
    void updateQuantity(@Param("userId") Long userId, @Param("productId") String productId, @Param("quantity") int quantity);
    void insertCartItem(@Param("userId") Long userId, @Param("productId") String productId, @Param("quantity") int quantity);

    /**
     * 新商品一覧取得（reservationDeadline がない商品のみ）
     *
     * @param categories 選択されたカテゴリ一覧
     * @return 新商品一覧
     */
    List<ProductInfo> selectNewProductList(@Param("categories") List<String> categories);

    /**
     * 予約商品一覧取得（reservationDeadline がある商品のみ）
     *
     * @param categories 選択されたカテゴリ一覧
     * @return 予約商品一覧
     */
    List<ProductInfo> selectReservationProductList(@Param("categories") List<String> categories);

    ProductInfo selectProductDetail(@Param("productId") Long productId);
    List<String> selectProductDetailImages(@Param("productId") Long productId);
    ProductDetails selectProductDetails(@Param("productId") Long productId);

    /**
     * カテゴリーグループ全件一覧取得
     */
    List<Categories> findAllCategoryGroupCode();

    /**
     * 予約商品用のカテゴリーグループのみ取得
     */
    List<Categories> findReservationCategoryGroup();

    /**
     * 選択されたカテゴリーグループ配下のカテゴリ情報を取得
     */
    List<CategoryDetails> findCategory(@Param("categoryGroupCodes") List<String> categoryGroupCode);
}
