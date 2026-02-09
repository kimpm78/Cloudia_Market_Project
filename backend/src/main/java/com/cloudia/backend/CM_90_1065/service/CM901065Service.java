package com.cloudia.backend.CM_90_1065.service;

import java.util.List;
import com.cloudia.backend.CM_90_1065.model.ProductCodeDto;

public interface CM901065Service {
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
    List<ProductCodeDto> findByProductCode(String searchTerm, int searchType);

    /**
     * 상품 코드 등록
     * 
     * @param entity 상품 코드 정보
     */
    Integer insCode(ProductCodeDto entity, String memberNumber);

    /**
     * 상품 코드 삭제
     * 
     * @param res 상품 코드 정보
     */
    Integer uptCode(List<ProductCodeDto> entity, String memberNumber);
}
