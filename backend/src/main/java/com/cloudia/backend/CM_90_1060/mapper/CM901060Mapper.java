package com.cloudia.backend.CM_90_1060.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1060.model.Attachments;
import com.cloudia.backend.CM_90_1060.model.Categories;
import com.cloudia.backend.CM_90_1060.model.CategoryDetails;
import com.cloudia.backend.CM_90_1060.model.ProductDetails;
import com.cloudia.backend.CM_90_1060.model.ProductUpt;
import com.cloudia.backend.CM_90_1060.model.Products;
import com.cloudia.backend.CM_90_1060.model.ResponseProducts;
import com.cloudia.backend.CM_90_1060.model.Stock;

@Mapper
public interface CM901060Mapper {

    /**
     * 商品一覧を取得
     * 
     * @return 商品一覧
     */
    List<ResponseProducts> findAllProduct();

    /**
     * 条件に一致する商品一覧を取得
     * 
     * @param searchTerm キーワード
     * @param searchType 種別（1:商品コード、2:商品名）
     * @return 商品一覧
     */
    List<ResponseProducts> findByProduct(@Param("searchTerm") String searchTerm, @Param("searchType") int searchType);

    /**
     * 商品を削除
     * 
     * @param productIds 削除対象IDリスト
     * @return 削除件数
     */
    Integer delProduct(@Param("productIds") List<Integer> productIds);

    /**
     * 画像を削除
     * 
     * @param productIds 削除対象IDリスト
     * @return 削除件数
     */
    Integer delAttachMents(@Param("productIds") List<Integer> productIds);

    /**
     * カテゴリグループコード一覧を取得
     * 
     * @return カテゴリグループコード一覧
     */
    List<Categories> findAllCategoryGroupCode();

    /**
     * 選択されたカテゴリグループの下位カテゴリ情報を取得
     * 
     * @param categoryGroupCode カテゴリグループコード
     * @return 下位カテゴリ情報
     */
    List<CategoryDetails> findCategory(@Param("categoryGroupCodes") List<String> categoryGroupCode);

    /**
     * 登録可能な在庫一覧を取得
     * 
     * @return 在庫一覧
     */
    List<Stock> findAllStockCode();

    /**
     * 次のproduct_id値を取得
     * 
     * @return 商品ID
     */
    long getNextProductId();

    /**
     * エディタ画像を登録
     * 
     * @param entity 登録する画像ファイル情報
     * @return 登録結果
     */
    int editorInsert(Attachments entity);

    /**
     * エディタ画像を更新
     * 
     * @param entity 更新する画像ファイル情報
     * @return 更新結果
     */
    int editorUpdate(Attachments entity);

    /**
     * エディタ画像を取得
     * 
     * @param productCode コード
     * @return 画像一覧
     */
    List<Attachments> editorGet(@Param("productCode") Long productCode);

    /**
     * 商品を登録
     * 
     * @param entity 登録する商品情報
     * @return 登録結果
     */
    int productInsert(Products entity);

    /**
     * 商品を更新
     * 
     * @param entity 更新する商品情報
     * @return 更新結果
     */
    int productUpdate(Products entity);

    /**
     * 商品詳細を登録
     * 
     * @param entity 保存する商品詳細情報
     * @return 登録結果
     */
    int insertProductDetail(ProductDetails entity);

    /**
     * 商品詳細を更新
     * 
     * @param entity 更新する商品詳細情報
     * @return 更新結果
     */
    int updateProductDetail(ProductDetails entity);

    /**
     * 登録有無を取得
     * 
     * @return 登録済み商品
     */
    ProductUpt findByUpdProductById(@Param("productCode") int productCode);

    /**
     * 登録有無を取得
     * 
     * @return 登録済み商品コード件数
     */
    int findByProductByCode(@Param("productCode") String productCode);
}
