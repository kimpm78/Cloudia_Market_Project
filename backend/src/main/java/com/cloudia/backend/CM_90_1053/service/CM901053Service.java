package com.cloudia.backend.CM_90_1053.service;

import java.util.List;

import com.cloudia.backend.CM_90_1053.model.Stocks;

public interface CM901053Service {
    /**
     * 在庫状況取得
     * 
     * @return 在庫状況リスト
     */
    List<Stocks> findAllStockStatus();

    /**
     * 選択された商品コード／商品名の在庫状況情報取得
     * 
     * @param searchType 検索タイプ（1: 商品コード 2: 商品名）
     * @param searchTerm 検索キーワード
     * @return 在庫状況情報
     */
    List<Stocks> findByStockStatus(String searchType, String searchTerm);
}