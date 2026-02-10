package com.cloudia.backend.CM_90_1053.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1053.model.Stocks;

@Mapper
public interface CM901053Mapper {
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
    List<Stocks> findByStockStatus(@Param("searchType") String searchType, @Param("searchTerm") String searchTerm);
}