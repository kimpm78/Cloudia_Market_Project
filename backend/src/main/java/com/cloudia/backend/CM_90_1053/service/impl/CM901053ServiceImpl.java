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

    // Mapper 정의
    private final CM901053Mapper cm901053Mapper;

    /**
     * 재고 현황 조회
     * 
     * @return 재고 현황 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<Stocks> findAllStockStatus() {
        List<Stocks> StocksList = cm901053Mapper.findAllStockStatus();
        log.info("조회된 재고 현황 : {}", StocksList == null ? 0 : StocksList.size());
        return StocksList;

    }

    /**
     * 선택 된 상품 코드 / 상품명의 재고 현황 정보 조회
     * 
     * @param searchType 검색 타입 (1: 상품 코드 2: 상품 명)
     * @param searchTerm 검색어
     * @return 재고 현황 정보
     */
    @Override
    @Transactional(readOnly = true)
    public List<Stocks> findByStockStatus(String searchType, String searchTerm) {
        List<Stocks> StocksList = cm901053Mapper.findByStockStatus(searchType, searchTerm);
        log.info("조회된 재고 현황 : {}", StocksList == null ? 0 : StocksList.size());
        return StocksList;
    }
}
