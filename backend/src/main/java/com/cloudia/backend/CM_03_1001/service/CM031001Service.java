package com.cloudia.backend.CM_03_1001.service;

import java.util.List;
import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_03_1001.model.CartRequest;
import com.cloudia.backend.CM_03_1001.model.Categories;
import com.cloudia.backend.CM_03_1001.model.CategoryDetails;
import com.cloudia.backend.CM_03_1001.model.CategoryGroupForCheckbox;
import com.cloudia.backend.CM_03_1001.model.ProductInfo;
import com.cloudia.backend.CM_03_1001.model.ResponseModel;

public interface CM031001Service {
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
     * 예약 상품 전체 목록 조회
     *
     * @param categories 카테고리 리스트
     * @return 상품 리스트
     */
    ResponseEntity<ResponseModel<List<ProductInfo>>> getReservationProductList(List<String> categories);

    /**
     * 특정 상품의 상세 정보를 조회
     *
     * <p>기본 상품 정보(ProductInfo)에 썸네일 URL, 상품 설명, 무게 등
     * ProductDetails의 추가 정보를 병합하여 반환 </p>
     *
     * @param productId 조회할 상품 ID
     * @return 해당 상품의 상세 정보 목록 (단일 건을 리스트로 감쌈)
     */
    ResponseEntity<ResponseModel<List<ProductInfo>>> getProductDetail(Long productId);

    /**
     * 장바구니에 상품 추가
     *
     * @param cartRequest 사용자 ID, 상품 ID, 수량
     * @return 처리 결과
     */
    ResponseEntity<ResponseModel<Void>> addToCart(CartRequest cartRequest);
}