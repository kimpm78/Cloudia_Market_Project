package com.cloudia.backend.CM_90_1063.service;

import java.util.List;
import java.util.Optional;

import com.cloudia.backend.CM_90_1063.model.ProductCode;
import com.cloudia.backend.CM_90_1063.model.Stock;
import com.cloudia.backend.CM_90_1063.model.StockInfo;

public interface CM901063Service {
    /**
     * 상품 코드 전체 리스트 조회
     * 
     * @return 상품 코드 전체 리스트
     */
    List<ProductCode> findAllProductCode();

    /**
     * 재고 입/출고 등록
     *
     * @param entity 등록 할 재고 정보 엔티티
     * 
     * @return 등록 성공 여부
     **/
    Integer stockUpsert(ProductCode entity, String userId);

    /**
     * 입/출고 일람 전체 조회
     * 
     * @return 입/출고 일람 전체 리스트
     */
    List<StockInfo> findAllStocks();

    /**
     * 선택 된 상품 코드 / 상품명의 상품 가격 정보 조회
     * 
     * @param searchType 검색 타입 (1: 상품 코드 2: 상품 명)
     * @param searchTerm 검색어
     * @return 상품 정보
     */
    List<StockInfo> findByStocks(String searchType, String searchTerm);

    /**
     * 특정 상품 코드 가격 조회
     * 
     * @param searchCode 검색 상품 코드
     * @return 상품 코드 리스트
     */
    Optional<Stock> getStockByCode(String searchCode);
}
