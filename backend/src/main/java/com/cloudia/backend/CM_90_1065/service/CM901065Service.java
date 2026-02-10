package com.cloudia.backend.CM_90_1065.service;

import java.util.List;
import com.cloudia.backend.CM_90_1065.model.ProductCodeDto;

public interface CM901065Service {
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
    List<ProductCodeDto> findByProductCode(String searchTerm, int searchType);

    /**
     * 商品コード登録
     * 
     * @param entity 商品コード情報
     */
    Integer insCode(ProductCodeDto entity, String memberNumber);

    /**
     * 商品コード削除
     * 
     * @param entity 商品コード情報
     */
    Integer uptCode(List<ProductCodeDto> entity, String memberNumber);
}
