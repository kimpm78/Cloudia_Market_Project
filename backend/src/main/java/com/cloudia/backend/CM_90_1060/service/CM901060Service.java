package com.cloudia.backend.CM_90_1060.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.cloudia.backend.CM_90_1060.model.Categories;
import com.cloudia.backend.CM_90_1060.model.CategoryDetails;
import com.cloudia.backend.CM_90_1060.model.ProductUpt;
import com.cloudia.backend.CM_90_1060.model.RequestModel;
import com.cloudia.backend.CM_90_1060.model.ResponseModel;
import com.cloudia.backend.CM_90_1060.model.ResponseProducts;
import com.cloudia.backend.CM_90_1060.model.Stock;

public interface CM901060Service {
    /**
     * 카테고리 그룹 코드 전체 리스트 조회
     * 
     * @return 카테고리 그룹 코드 전체 리스트
     */
    ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode();

    /**
     * 선택 된 카테고리 그룹의 하위 카테고리 정보 조회
     * 
     * @param categoryGroupCode 카테고리 그룹 코드
     * @return 하위 카테고리 정보
     */
    ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(List<String> categoryGroupCode);

    /**
     * 등록 가능한 재고 리스트 조회
     * 
     * @return 재고 리스트
     */
    ResponseEntity<ResponseModel<List<Stock>>> findAllStockCode();

    /**
     * 특정 상품 조회
     * 
     * @param productId 상품 코드
     * @return 특정 상품 리스트
     */
    ResponseEntity<ResponseModel<ProductUpt>> findByProductCode(int productId);

    /**
     * 상품 등록
     * 
     * @param entity 등록 할 상품 정보
     * @return 상품 여부
     */
    ResponseEntity<ResponseModel<Integer>> productUpload(@ModelAttribute RequestModel entity, String userId);

    /**
     * 상품 수정
     * 
     * @param entity 수정 할 상품 정보
     * @return 상품 수정 여부
     */
    ResponseEntity<ResponseModel<Integer>> productUpdate(@ModelAttribute RequestModel entity, String userId);

    /**
     * 상품 전체 리스트 조회
     * 
     * @return 상품 전체 리스트
     */
    ResponseEntity<ResponseModel<List<ResponseProducts>>> findAllProduct();

    /**
     * 특정 상품 리스트 조회
     * 
     * @param searchTerm 키워드
     * @param searchType 타입 (1:상품 코드, 2:상품 명)
     * @return 상품 리스트
     */
    ResponseEntity<ResponseModel<List<ResponseProducts>>> getFindProduct(String searchTerm, int searchType);

    /**
     * 상품 삭제
     * 
     * @param productIds 삭제 아이디 리스트
     * @return 삭제 여부
     */
    ResponseEntity<ResponseModel<Integer>> delProduct(List<Integer> productIds, String userId);

    /**
     * 이미지 등록
     * 
     * @param file 등록 할 이미지 정보
     * @return 등록 여부
     */
    ResponseEntity<ResponseModel<String>> imageUpload(MultipartFile file);
}
