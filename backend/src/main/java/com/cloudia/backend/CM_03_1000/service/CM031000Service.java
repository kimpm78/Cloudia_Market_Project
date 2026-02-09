package com.cloudia.backend.CM_03_1000.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.cloudia.backend.CM_03_1000.model.CartRequest;
import com.cloudia.backend.CM_03_1000.model.Categories;
import com.cloudia.backend.CM_03_1000.model.CategoryDetails;
import com.cloudia.backend.CM_03_1000.model.CategoryGroupForCheckbox;
import com.cloudia.backend.CM_03_1000.model.ProductInfo;
import com.cloudia.backend.common.model.ResponseModel;

public interface CM031000Service {
    /**
     * 카테고리 그룹 코드 전체 리스트 조회
     * 
     * @return 카테고리 그룹 코드 전체 리스트
     */
    ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode();

    /**
     * 선택 된 카테고리 그룹의 하위 카테고리 정보 조회
     * 
     * @param categoryGroupCode 카테고리 그룹 코드드
     * @return 하위 카테고리 정보
     */
    ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(List<String> categoryGroupCode);

    /**
     * 체크박스용 카테고리 그룹 + 하위 카테고리 목록 조회
     *
     * @return 그룹 + 카테고리 리스트
     */
    ResponseEntity<ResponseModel<List<CategoryGroupForCheckbox>>> findAllCategoryGroupForCheckbox();


    /**
     * 신상품 전체 목록 조회
     *
     * @param categories 카테고리 리스트
     * @return 상품 리스트
     */
    ResponseEntity<ResponseModel<List<ProductInfo>>> getProductList(List<String> categories);

    /**
     * 특정 상품 상세 조회
     *
     * @param productId 상품 ID
     * @return 해당 상품 정보
     */
    ResponseEntity<ResponseModel<List<ProductInfo>>> getProductDetail(Long productId);

    /**
     * 이미지 등록
     * 
     * @param file 등록 할 이미지 정보
     * @return 등록 여부
     */
    ResponseEntity<ResponseModel<String>> imageUpload(MultipartFile file);

    /**
     * 장바구니에 상품 추가
     *
     * @param cartRequest 사용자 ID, 상품 ID, 수량
     * @return 처리 결과
     */
    ResponseEntity<ResponseModel<Void>> addToCart(CartRequest cartRequest);
}