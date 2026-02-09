package com.cloudia.backend.CM_90_1053.service;

import java.util.List;

import com.cloudia.backend.CM_90_1053.model.Stocks;

public interface CM901053Service {
    /**
     * 재고 현황 조회
     * 
     * @return 재고 현황 리스트
     */
    List<Stocks> findAllStockStatus();

    /**
     * 선택 된 상품 코드 / 상품명의 재고 현황 정보 조회
     * 
     * @param searchType 검색 타입 (1: 상품 코드 2: 상품 명)
     * @param searchTerm 검색어
     * @return 재고 현황 정보
     */
    List<Stocks> findByStockStatus(String searchType, String searchTerm);
}
