package com.cloudia.backend.CM_90_1065.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1065.model.ProductCodeDto;

@Mapper
public interface CM901065Mapper {
    /**
     * 상품 코드 조회
     * 
     * @return 상품코드 리스트
     */
    List<ProductCodeDto> getProductCode();

    /**
     * 상품 코드 조회
     * 
     * @param searchTerm 키워드
     * @param searchType 타입 (1:상품 코드, 2:상품명)
     * @return 유저 리스트
     */
    List<ProductCodeDto> findByProductCode(@Param("searchTerm") String searchTerm, @Param("searchType") int searchType);

    /**
     * 상품 코드 조회
     * 
     * @param searchTerm 키워드
     * @param searchType 타입 (1:상품 코드, 2:상품명)
     * @return 유저 리스트
     */
    ProductCodeDto findByOneCode(@Param("searchTerm") String searchTerm, @Param("searchType") int searchType,
            @Param("category") String category);

    /**
     * 상품 코드 등록
     * 
     * @param entity 상품 코드 정보
     */
    Integer insCode(ProductCodeDto entity);

    /**
     * 상품 코드 삭제
     * 
     * @param res 상품 코드 정보
     */
    Integer uptCode(ProductCodeDto entity);

    /**
     * 재고 조회
     * 
     * @param searchTerm 키워드
     * @return 유저 리스트
     */
    ProductCodeDto findByOneStock(@Param("searchTerm") String searchTerm);

    /**
     * 상품 조회
     * 
     * @param searchTerm 키워드
     * @return 유저 리스트
     */
    ProductCodeDto findByOneProduct(@Param("searchTerm") String searchTerm);
}
