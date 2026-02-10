package com.cloudia.backend.CM_90_1063.service;

import java.util.List;
import java.util.Optional;

import com.cloudia.backend.CM_90_1063.model.ProductCode;
import com.cloudia.backend.CM_90_1063.model.Stock;
import com.cloudia.backend.CM_90_1063.model.StockInfo;

public interface CM901063Service {
    /**
     * 商品コード全件リスト取得
     *
     * @return 商品コード全件リスト
     */
    List<ProductCode> findAllProductCode();

    /**
     * 在庫入出庫登録（登録/更新）
     *
     * @param entity 登録する在庫情報エンティティ
     * @param userId 実行ユーザーID
     * @return 登録成功可否
     **/
    Integer stockUpsert(ProductCode entity, String userId);

    /**
     * 入出庫一覧全件取得
     *
     * @return 入出庫一覧全件リスト
     */
    List<StockInfo> findAllStocks();

    /**
     * 選択した商品コード／商品名に紐づく商品価格情報取得
     *
     * @param searchType 検索タイプ（1: 商品コード 2: 商品名）
     * @param searchTerm 検索ワード
     * @return 商品情報
     */
    List<StockInfo> findByStocks(String searchType, String searchTerm);

    /**
     * 指定した商品コードの価格取得
     *
     * @param searchCode 検索商品コード
     * @return 商品情報（任意）
     */
    Optional<Stock> getStockByCode(String searchCode);
}
