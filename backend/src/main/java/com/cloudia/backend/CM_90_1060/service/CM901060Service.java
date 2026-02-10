package com.cloudia.backend.CM_90_1060.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_90_1060.model.Categories;
import com.cloudia.backend.CM_90_1060.model.CategoryDetails;
import com.cloudia.backend.CM_90_1060.model.ProductUpt;
import com.cloudia.backend.CM_90_1060.model.RequestModel;
import com.cloudia.backend.CM_90_1060.model.ResponseProducts;
import com.cloudia.backend.CM_90_1060.model.Stock;

public interface CM901060Service {
    /**
     * カテゴリグループコードの全リストを取得
     * 
     * @return カテゴリグループコードの全リスト
     */
    ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode();

    /**
     * 選択したカテゴリグループの下位カテゴリ情報を取得
     * 
     * @param categoryGroupCode カテゴリグループコード
     * @return 下位カテゴリ情報
     */
    ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(List<String> categoryGroupCode);

    /**
     * 登録可能な在庫リストを取得
     * 
     * @return 在庫リスト
     */
    ResponseEntity<ResponseModel<List<Stock>>> findAllStockCode();

    /**
     * 特定商品の取得
     * 
     * @param productId 商品コード
     * @return 特定商品
     */
    ResponseEntity<ResponseModel<ProductUpt>> findByProductCode(int productId);

    /**
     * 商品登録
     * 
     * @param entity 登録する商品情報
     * @return 登録結果
     */
    ResponseEntity<ResponseModel<Integer>> productUpload(@ModelAttribute RequestModel entity, String userId);

    /**
     * 商品更新
     * 
     * @param entity 更新する商品情報
     * @return 更新結果
     */
    ResponseEntity<ResponseModel<Integer>> productUpdate(@ModelAttribute RequestModel entity, String userId);

    /**
     * 商品の全リストを取得
     * 
     * @return 商品の全リスト
     */
    ResponseEntity<ResponseModel<List<ResponseProducts>>> findAllProduct();

    /**
     * 条件に一致する商品リストを取得
     * 
     * @param searchTerm キーワード
     * @param searchType 種別（1: 商品コード, 2: 商品名）
     * @return 商品リスト
     */
    ResponseEntity<ResponseModel<List<ResponseProducts>>> getFindProduct(String searchTerm, int searchType);

    /**
     * 商品削除
     * 
     * @param productIds 削除対象IDリスト
     * @return 削除結果
     */
    ResponseEntity<ResponseModel<Integer>> delProduct(List<Integer> productIds, String userId);

    /**
     * 画像登録
     * 
     * @param file 登録する画像ファイル
     * @return 登録結果
     */
    ResponseEntity<ResponseModel<String>> imageUpload(MultipartFile file);
}
