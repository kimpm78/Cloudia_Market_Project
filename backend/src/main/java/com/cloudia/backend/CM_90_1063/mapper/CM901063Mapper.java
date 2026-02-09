package com.cloudia.backend.CM_90_1063.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1063.model.ProductCode;
import com.cloudia.backend.CM_90_1063.model.Stock;
import com.cloudia.backend.CM_90_1063.model.StockDetail;
import com.cloudia.backend.CM_90_1063.model.StockInfo;

@Mapper
public interface CM901063Mapper {
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
    Optional<Stock> findByProductCode(String productCode);

    /**
     * 재고 입/출고 등록
     *
     * @param entity 등록 할 재고 정보 엔티티
     * 
     * @return 등록 성공 여부
     **/
    String stockUpsert(ProductCode entity);

    /**
     * 재고 등록
     * 
     * @param entity 재고 정보
     * @return 등록 여부
     */
    int insertStock(Stock entity);

    /**
     * 재고 업데이트
     * 
     * @param productOpt 업데이트 재고 정보
     * @param entity     재고 정보
     * @return 업데이트 여부
     */
    int updateStock(Stock entity);

    /**
     * 재고 상세 등록
     * 
     * @param entity  재고 정보
     * @param stockId 재고 아이디
     * @return 등록 여부
     */
    int insertStockDetail(StockDetail entity);

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
    List<StockInfo> findByStocks(@Param("searchType") String searchType, @Param("searchTerm") String searchTerm);

}
