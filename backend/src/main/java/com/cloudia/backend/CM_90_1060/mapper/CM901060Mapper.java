package com.cloudia.backend.CM_90_1060.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1060.model.Attachments;
import com.cloudia.backend.CM_90_1060.model.Categories;
import com.cloudia.backend.CM_90_1060.model.CategoryDetails;
import com.cloudia.backend.CM_90_1060.model.ProductDetails;
import com.cloudia.backend.CM_90_1060.model.ProductUpt;
import com.cloudia.backend.CM_90_1060.model.Products;
import com.cloudia.backend.CM_90_1060.model.ResponseProducts;
import com.cloudia.backend.CM_90_1060.model.Stock;

@Mapper
public interface CM901060Mapper {

    /**
     * 상품 전체 리스트 조회
     * 
     * @return 상품 전체 리스트
     */
    List<ResponseProducts> findAllProduct();

    /**
     * 특정 상품 리스트 조회
     * 
     * @param searchTerm 키워드
     * @param searchType 타입 (1:상품 코드, 2:상품 명)
     * @return 상품 리스트
     */
    List<ResponseProducts> findByProduct(@Param("searchTerm") String searchTerm, @Param("searchType") int searchType);

    /**
     * 상품 삭제
     * 
     * @param productIds 삭제 아이디 리스트
     * @return 삭제 여부
     */
    Integer delProduct(@Param("productIds") List<Integer> productIds);

    /**
     * 이미지 삭제
     * 
     * @param productIds 삭제 아이디 리스트
     * @return 삭제 여부
     */
    Integer delAttachMents(@Param("productIds") List<Integer> productIds);

    /**
     * 카테고리 그룹 코드 전체 리스트 조회
     * 
     * @return 카테고리 그룹 코드 전체 리스트
     */
    List<Categories> findAllCategoryGroupCode();

    /**
     * 선택 된 카테고리 그룹의 하위 카테고리 정보 조회
     * 
     * @param categoryGroupCode 카테고리 그룹 코드드
     * @return 하위 카테고리 정보
     */
    List<CategoryDetails> findCategory(@Param("categoryGroupCodes") List<String> categoryGroupCode);

    /**
     * 등록 가능한 재고 리스트 조회
     * 
     * @return 재고 리스트
     */
    List<Stock> findAllStockCode();

    /**
     * 다음 product_id 값 조회
     * 
     * @return 상품 아이디
     */
    long getNextProductId();

    /**
     * 에디터 이미지 등록
     * 
     * @param entity 등록 할 이미지 파일 정보
     * @return 등록 여부
     */
    int editorInsert(Attachments entity);

    /**
     * 에디터 이미지 수정
     * 
     * @param entity 등록 할 이미지 파일 정보
     * @return 등록 여부
     */
    int editorUpdate(Attachments entity);

    /**
     * 에디터 이미지 조회
     * 
     * @param productCode 코드
     * @return 이미지
     */
    List<Attachments> editorGet(@Param("productCode") Long productCode);

    /**
     * 상품 등록
     * 
     * @param entity 등록 할 상품 정보
     * @return 등록 여부
     */
    int productInsert(Products entity);

    /**
     * 상품 수정
     * 
     * @param entity 수정 할 상품 정보
     * @return 수정 여부
     */
    int productUpdate(Products entity);

    /**
     * 상품 상세 등록
     * 
     * @param entity 저장될 상품 상세 정보
     * @return 등록 결과
     */
    int insertProductDetail(ProductDetails entity);

    /**
     * 상품 상세 수정
     * 
     * @param entity 수정될 상품 상세 정보
     * @return 수정 결과
     */
    int updateProductDetail(ProductDetails entity);

    /**
     * 등록 여부 조회
     * 
     * @return 등록된 상품
     */
    ProductUpt findByUpdProductById(@Param("productCode") int productCode);

    /**
     * 등록 여부 조회
     * 
     * @return 등록된 상품코드 카운트
     */
    int findByProductByCode(@Param("productCode") String productCode);
}
