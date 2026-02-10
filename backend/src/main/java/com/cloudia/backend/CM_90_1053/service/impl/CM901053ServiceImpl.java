package com.cloudia.backend.CM_90_1053.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_90_1053.mapper.CM901053Mapper;
import com.cloudia.backend.CM_90_1053.model.Stocks;
import com.cloudia.backend.CM_90_1053.service.CM901053Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM901053ServiceImpl implements CM901053Service {

    // Mapper定義
    private final CM901053Mapper cm901053Mapper;

    /**
     * 在庫状況取得
     * 
     * @return 在庫状況リスト
     */
    @Override
    @Transactional(readOnly = true)
    public List<Stocks> findAllStockStatus() {
        List<Stocks> StocksList = cm901053Mapper.findAllStockStatus();
        log.info("取得した在庫状況: {}", StocksList == null ? 0 : StocksList.size());
        return StocksList;

    }

    /**
     * 選択された商品コード／商品名の在庫状況情報取得
     * 
     * @param searchType 検索タイプ（1: 商品コード 2: 商品名）
     * @param searchTerm 検索キーワード
     * @return 在庫状況情報
     */
    @Override
    @Transactional(readOnly = true)
    public List<Stocks> findByStockStatus(String searchType, String searchTerm) {
        List<Stocks> StocksList = cm901053Mapper.findByStockStatus(searchType, searchTerm);
        log.info("取得した在庫状況: {}", StocksList == null ? 0 : StocksList.size());
        return StocksList;
    }
}
