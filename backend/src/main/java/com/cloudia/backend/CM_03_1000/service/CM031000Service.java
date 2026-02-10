package com.cloudia.backend.CM_03_1000.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.cloudia.backend.CM_03_1000.model.CartRequest;
import com.cloudia.backend.CM_03_1000.model.Categories;
import com.cloudia.backend.CM_03_1000.model.CategoryDetails;
import com.cloudia.backend.CM_03_1000.model.CategoryGroupForCheckbox;
import com.cloudia.backend.CM_03_1000.model.ProductInfo;
import com.cloudia.backend.common.model.ResponseModel;

public interface CM031000Service {
    /**
     * カテゴリグループコード一覧取得
     * 
     * @return カテゴリグループコード一覧
     */
    ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode();

    /**
     * 選択したカテゴリグループの下位カテゴリ情報取得
     * 
     * @param categoryGroupCode カテゴリグループコード
     * @return 下位カテゴリ情報
     */
    ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(List<String> categoryGroupCode);

    /**
     * チェックボックス用：カテゴリグループ＋下位カテゴリ一覧取得
     *
     * @return グループ＋カテゴリ一覧
     */
    ResponseEntity<ResponseModel<List<CategoryGroupForCheckbox>>> findAllCategoryGroupForCheckbox();


    /**
     * 新商品一覧取得
     *
     * @param categories カテゴリ一覧
     * @return 商品一覧
     */
    ResponseEntity<ResponseModel<List<ProductInfo>>> getProductList(List<String> categories);

    /**
     * 商品詳細取得
     *
     * @param productId 商品ID
     * @return 対象商品の情報
     */
    ResponseEntity<ResponseModel<List<ProductInfo>>> getProductDetail(Long productId);

    /**
     * 画像登録
     * 
     * @param file 登録する画像情報
     * @return 登録結果
     */
    ResponseEntity<ResponseModel<String>> imageUpload(MultipartFile file);

    /**
     * カートに商品を追加
     *
     * @param cartRequest ユーザーID、商品ID、数量
     * @return 処理結果
     */
    ResponseEntity<ResponseModel<Void>> addToCart(CartRequest cartRequest);
}