package com.cloudia.backend.CM_90_1065.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1065.model.ProductCodeDto;

@Mapper
public interface CM901065Mapper {
    /**
     * 商品コード取得
     * 
     * @return 商品コード一覧
     */
    List<ProductCodeDto> getProductCode();

    /**
     * 商品コード検索
     * 
     * @param searchTerm キーワード
     * @param searchType 種別（1:商品コード、2:商品名）
     * @return 商品コード一覧
     */
    List<ProductCodeDto> findByProductCode(@Param("searchTerm") String searchTerm, @Param("searchType") int searchType);

    /**
     * 商品コード取得（単件）
     * 
     * @param searchTerm キーワード
     * @param searchType 種別（1:商品コード、2:商品名）
     * @return 商品コード
     */
    ProductCodeDto findByOneCode(@Param("searchTerm") String searchTerm, @Param("searchType") int searchType,
            @Param("category") String category);

    /**
     * 商品コード登録
     * 
     * @param entity 商品コード情報
     */
    Integer insCode(ProductCodeDto entity);

    /**
     * 商品コード更新
     * 
     * @param entity 商品コード情報
     */
    Integer uptCode(ProductCodeDto entity);

    /**
     * 在庫取得
     * 
     * @param searchTerm キーワード
     * @return 在庫情報
     */
    ProductCodeDto findByOneStock(@Param("searchTerm") String searchTerm);

    /**
     * 商品取得
     * 
     * @param searchTerm キーワード
     * @return 商品情報
     */
    ProductCodeDto findByOneProduct(@Param("searchTerm") String searchTerm);
}
