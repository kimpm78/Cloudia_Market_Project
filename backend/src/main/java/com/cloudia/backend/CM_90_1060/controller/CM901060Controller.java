package com.cloudia.backend.CM_90_1060.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cloudia.backend.CM_90_1060.model.Categories;
import com.cloudia.backend.CM_90_1060.model.CategoryDetails;
import com.cloudia.backend.CM_90_1060.model.ProductUpt;
import com.cloudia.backend.CM_90_1060.model.RequestModel;
import com.cloudia.backend.CM_90_1060.model.ResponseModel;
import com.cloudia.backend.CM_90_1060.model.ResponseProducts;
import com.cloudia.backend.CM_90_1060.model.Stock;
import com.cloudia.backend.CM_90_1060.service.CM901060Service;
import com.cloudia.backend.config.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin")
public class CM901060Controller {
    // Service 정의
    private final CM901060Service cm901060Service;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 상품 전체 리스트 조회
     * 
     * @return 상품 전체 리스트
     */
    @GetMapping("/product/findAll")
    public ResponseEntity<ResponseModel<List<ResponseProducts>>> getfindAllProduct() {
        return cm901060Service.findAllProduct();
    }

    /**
     * 특정 상품 리스트 조회
     * 
     * @param searchTerm 키워드
     * @param searchType 타입 (1:상품 코드, 2:상품 명)
     * @return 상품 리스트
     */
    @GetMapping("/product/findProduct")
    public ResponseEntity<ResponseModel<List<ResponseProducts>>> getFindProduct(@RequestParam String searchTerm,
            @RequestParam int searchType) {
        return cm901060Service.getFindProduct(searchTerm, searchType);
    }

    /**
     * 특정 상품 조회
     * 
     * @param productId 상품 코드
     * @return 특정 상품 리스트
     */
    @GetMapping("/product/findByProductCode")
    public ResponseEntity<ResponseModel<ProductUpt>> findByProductCode(@RequestParam int productId) {
        return cm901060Service.findByProductCode(productId);
    }

    /**
     * 상품 삭제
     * 
     * @param productIds 삭제 아이디 리스트
     * @return 삭제 여부
     */
    @DeleteMapping("/product/del")
    public ResponseEntity<ResponseModel<Integer>> delProduct(@RequestBody List<Integer> productIds,
            HttpServletRequest request) {
        String userId = jwtTokenProvider
                .getUserIdFromToken(jwtTokenProvider.resolveToken(request))
                .toString();
        return cm901060Service.delProduct(productIds, userId);
    }

    /**
     * 카테고리 그룹 코드 전체 리스트 조회
     * 
     * @return 카테고리 그룹 코드 전체 리스트
     */
    @GetMapping("/product/categoryGroupCode")
    public ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode() {
        log.info("카테고리 그룹 코드 리스트 조회 Start");
        ResponseEntity<ResponseModel<List<Categories>>> response = cm901060Service.findAllCategoryGroupCode();
        log.info("카테고리 그룹 코드 리스트 조회 End");
        return response;
    }

    /**
     * 등록 가능한 재고 리스트 조회
     * 
     * @return 재고 리스트
     */
    @GetMapping("/product/stockCode")
    public ResponseEntity<ResponseModel<List<Stock>>> findAllStockCode() {
        log.info("재고 코드 리스트 조회 Start");
        ResponseEntity<ResponseModel<List<Stock>>> response = cm901060Service.findAllStockCode();
        log.info("재고 코드 리스트 조회 End");
        return response;
    }

    /**
     * 선택 된 카테고리 그룹의 하위 카테고리 정보 조회
     * 
     * @param categoryGroupCode 카테고리 그룹 코드드
     * @return 하위 카테고리 정보
     */
    @PostMapping("/product/findCategory")
    public ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(
            @RequestBody List<String> categoryGroupCodes) {
        log.info("특정 하위 카테고리 정보 조회 Start");
        ResponseEntity<ResponseModel<List<CategoryDetails>>> response = cm901060Service
                .findCategory(categoryGroupCodes);
        log.info("특정 하위 카테고리 정보 조회 End");
        return response;
    }

    /**
     * 상품 등록
     * 
     * @param entity 등록 할 상품 정보
     * @return 상품 여부
     */
    @PostMapping("/product/upload")
    public ResponseEntity<ResponseModel<Integer>> productUpload(@ModelAttribute RequestModel entity,
            HttpServletRequest request) {
        String userId = jwtTokenProvider
                .getUserIdFromToken(jwtTokenProvider.resolveToken(request))
                .toString();
        return cm901060Service.productUpload(entity, userId);
    }

    /**
     * 상품 수정
     * 
     * @param entity 수정 할 상품 정보
     * @return 상품 수정 여부
     */
    @PostMapping("/product/update")
    public ResponseEntity<ResponseModel<Integer>> productUpdate(@ModelAttribute RequestModel entity,
            HttpServletRequest request) {
        String userId = jwtTokenProvider
                .getUserIdFromToken(jwtTokenProvider.resolveToken(request))
                .toString();
        return cm901060Service.productUpdate(entity, userId);
    }

    /**
     * 이미지 등록
     * 
     * @param file 등록 할 이미지 정보
     * @return 등록 여부
     */
    @PostMapping("/product/image/upload")
    public ResponseEntity<ResponseModel<String>> postImageUpload(@RequestBody MultipartFile file) {

        return cm901060Service.imageUpload(file);
    }
}
