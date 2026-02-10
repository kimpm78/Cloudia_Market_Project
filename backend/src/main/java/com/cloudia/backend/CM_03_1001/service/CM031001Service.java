package com.cloudia.backend.CM_03_1001.service;

import java.util.List;
import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_03_1001.model.CartRequest;
import com.cloudia.backend.CM_03_1001.model.Categories;
import com.cloudia.backend.CM_03_1001.model.CategoryDetails;
import com.cloudia.backend.CM_03_1001.model.CategoryGroupForCheckbox;
import com.cloudia.backend.CM_03_1001.model.ProductInfo;
import com.cloudia.backend.common.model.ResponseModel;

public interface CM031001Service {
    /**
     * カテゴリーグループコード一覧取得
     * 
     * @return カテゴリーグループコード一覧
     */
    ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode();

    /**
     * 選択したカテゴリーグループの下位カテゴリー情報取得
     * 
     * @param categoryGroupCode カテゴリーグループコード
     * @return 下位カテゴリー情報
     */
    ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(List<String> categoryGroupCode);

    /**
     * チェックボックス用：カテゴリーグループ＋下位カテゴリー一覧取得
     *
     * @return グループ＋カテゴリー一覧
     */
    ResponseEntity<ResponseModel<List<CategoryGroupForCheckbox>>> findAllCategoryGroupForCheckbox();

    /**
     * 新商品一覧取得
     *
     * @param categories カテゴリー一覧
     * @return 商品一覧
     */
    ResponseEntity<ResponseModel<List<ProductInfo>>> getProductList(List<String> categories);

    /**
     * 予約商品一覧取得
     *
     * @param categories カテゴリー一覧
     * @return 商品一覧
     */
    ResponseEntity<ResponseModel<List<ProductInfo>>> getReservationProductList(List<String> categories);

    /**
     * 特定商品の詳細情報取得
     *
     * @param productId 取得対象の商品ID
     * @return 該当商品の詳細情報一覧（単一件をリストで返却）
     */
    ResponseEntity<ResponseModel<List<ProductInfo>>> getProductDetail(Long productId);

    /**
     * カートに商品を追加
     *
     * @param cartRequest ユーザーID、商品ID、数量
     * @return 処理結果
     */
    ResponseEntity<ResponseModel<Void>> addToCart(CartRequest cartRequest);
}