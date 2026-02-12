package com.cloudia.backend.CM_03_1003.service;

import java.util.List;
import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_03_1003.model.CartRequest;
import com.cloudia.backend.CM_03_1003.model.ProductInfo;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_03_1003.model.Categories;
import com.cloudia.backend.CM_03_1003.model.CategoryDetails;
import com.cloudia.backend.CM_03_1003.model.CategoryGroupForCheckbox;

public interface CM031003Service {
    /**
     * カテゴリグループコード一覧を取得
     *
     * @return カテゴリグループコード一覧
     */
    ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode();

    /**
     * 選択したカテゴリグループの下位カテゴリ情報を取得
     *
     * @param categoryGroupCode カテゴリグループコード一覧
     * @return 下位カテゴリ情報
     */
    ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(List<String> categoryGroupCode);

    /**
     * チェックボックス用：カテゴリグループ＋下位カテゴリ一覧を取得
     *
     * @return グループ＋カテゴリ一覧
     */
    ResponseEntity<ResponseModel<List<CategoryGroupForCheckbox>>> findAllCategoryGroupForCheckbox();


    /**
     * 商品一覧を取得
     *
     * @param categories カテゴリ一覧
     * @return 商品一覧
     */
    ResponseEntity<ResponseModel<List<ProductInfo>>> getProductList(List<String> categories);

    /**
     * 商品詳細を取得
     *
     * @param productId 商品ID
     * @return 該当商品の情報
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