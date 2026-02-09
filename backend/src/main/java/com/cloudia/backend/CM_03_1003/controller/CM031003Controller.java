package com.cloudia.backend.CM_03_1003.controller;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.CM_03_1003.constants.CM031003MessageConstant;
import com.cloudia.backend.CM_03_1003.model.CartRequest;
import com.cloudia.backend.CM_03_1003.model.Categories;
import com.cloudia.backend.CM_03_1003.model.CategoryDetails;
import com.cloudia.backend.CM_03_1003.model.CategoryGroupForCheckbox;
import com.cloudia.backend.CM_03_1003.model.CategoryItem;
import com.cloudia.backend.CM_03_1003.model.ProductInfo;
import com.cloudia.backend.CM_03_1003.model.ResponseModel;
import com.cloudia.backend.CM_03_1003.service.CM031003Service;

import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/guest")
@CrossOrigin(origins = "*")
public class CM031003Controller {
    // Service 정의
    private final CM031003Service cm031003Service;

    // 상품 목록
    /**
     * 신상품 전체 목록 조회
     *
     * @return 상품 리스트
     */
    @GetMapping("/genres")
    public ResponseEntity<ResponseModel<List<ProductInfo>>> getProductList(
        @RequestParam(name = "categories", required = false) List<String> categories
    ) {
        log.info(CM031003MessageConstant.PRODUCT_FIND_ALL_START);
        log.info("카테고리 필터: {}", categories);

        ResponseEntity<ResponseModel<List<ProductInfo>>> response = cm031003Service.getProductList(categories);

        if (response == null || response.getBody() == null) {
            log.warn(CM031003MessageConstant.FAIL_PRODUCT_LIST_FETCH_NULL);
            return ResponseEntity.internalServerError()
                .body(setResponseDto(null, false, CM031003MessageConstant.FAIL_PRODUCT_LIST_FETCH_NULL));
        }

        List<ProductInfo> products = response.getBody().getResultList();

        if (products == null) {
            log.warn(CM031003MessageConstant.FAIL_PRODUCT_LIST_EMPTY);
            return ResponseEntity.ok(setResponseDto(Collections.emptyList(), true, CM031003MessageConstant.FAIL_PRODUCT_LIST_EMPTY));
        }

        log.info(CM031003MessageConstant.PRODUCT_FIND_ALL_COMPLETE, products.size());
        return ResponseEntity.ok(setResponseDto(products, true, CM031003MessageConstant.SUCCESS_PRODUCT_FIND));
    }

    // 상품 상세
    /**
     * 상품 상세 조회
     *
     * @param detailId 상품 ID
     * @return 상품 상세 정보
     */
    @GetMapping("/genres/{detailId}")
    public ResponseEntity<ResponseModel<ProductInfo>> getProductDetail(@PathVariable Long detailId) {
        log.info(CM031003MessageConstant.PRODUCT_FIND_BY_ID_START, detailId);
        List<ProductInfo> list = cm031003Service.getProductDetail(detailId).getBody().getResultList();
        ProductInfo product = (list != null && !list.isEmpty()) ? list.get(0) : null;
        log.info(CM031003MessageConstant.PRODUCT_FIND_BY_ID_COMPLETE, detailId, product != null ? 1 : 0);
        return ResponseEntity.ok(setResponseDto(product, true, CM031003MessageConstant.SUCCESS_PRODUCT_FIND));
    }
    
    /**
     * 카테고리 그룹 코드 전체 리스트 조회
     *
     * @return 카테고리 그룹 코드 전체 리스트
     */
    @GetMapping("/genres/categoryGroupCode")
    public ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode() {
        log.info(CM031003MessageConstant.CATEGORY_GROUP_FETCH_START);
        ResponseEntity<ResponseModel<List<Categories>>> response = cm031003Service.findAllCategoryGroupCode();
        log.info(CM031003MessageConstant.CATEGORY_GROUP_FETCH_SUCCESS);
        return response;
    }

    /**
     * 선택 된 카테고리 그룹의 하위 카테고리 정보 조회
     *
     * @param categoryGroupCodes 카테고리 그룹 코드 리스트
     * @return 하위 카테고리 정보
     */
    @PostMapping("/genres/findCategory")
    public ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(
            @RequestBody List<String> categoryGroupCodes) {
        log.info(CM031003MessageConstant.CATEGORY_DETAIL_FETCH_START, categoryGroupCodes);
        ResponseEntity<ResponseModel<List<CategoryDetails>>> response = cm031003Service
                .findCategory(categoryGroupCodes);
        log.info(CM031003MessageConstant.CATEGORY_DETAIL_FETCH_SUCCESS);
        return response;
    }
    
    /**
     * 체크박스용 카테고리 그룹 + 상세 목록 API
     */
    @GetMapping("/genres/categoryGroupForCheckbox")
    public ResponseEntity<ResponseModel<List<CategoryGroupForCheckbox>>> getCategoryGroupForCheckbox() {
        log.info("체크박스용 카테고리 그룹 호출 시작");

        List<Categories> rawGroups = cm031003Service.findAllCategoryGroupCode().getBody().getResultList();

        List<CategoryGroupForCheckbox> result = rawGroups.stream()
            .map((Categories group) -> {
                List<CategoryItem> items = group.getDetails().stream()
                    .map((CategoryDetails detail) -> CategoryItem.builder()
                        .code(detail.getCategoryCode())
                        .name(detail.getCategoryName())
                        .build())
                    .collect(Collectors.toList());

                return CategoryGroupForCheckbox.builder()
                    .groupCode(group.getCategoryGroupCode())
                    .groupName(group.getCategoryGroupName())
                    .categories(items)
                    .build();
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(setResponseDto(result, true, "카테고리 그룹+항목 목록 반환 완료"));
    }

    
    // 장바구니 추가
    /**
     * 장바구니에 상품 추가
     *
     * @param cartRequest 장바구니 요청
     * @param bindingResult 유효성 검사 결과
     * @return 장바구니 추가 결과
     */
    @PostMapping("/genres/cart")
    public ResponseEntity<ResponseModel<Void>> addToCart(@RequestBody @Valid CartRequest cartRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("\r\n "));
            log.warn(CM031003MessageConstant.FAIL_CART_ADD, errorMessage);
            return ResponseEntity.badRequest().body(setResponseDto(null, false, errorMessage));
        }

        log.info(CM031003MessageConstant.SUCCESS_CART_ADD);
        cm031003Service.addToCart(cartRequest);
        log.info(CM031003MessageConstant.SUCCESS_CART_UPDATE);
        return ResponseEntity.ok(setResponseDto(null, true, CM031003MessageConstant.SUCCESS_CART_ADD));
    }

    /**
     * 공통 응답 포맷 설정
     *
     * @param resultList 결과 데이터
     * @param ret        성공 여부
     * @param msg        메시지
     * @return 공통 응답 모델
     */
    private <T> ResponseModel<T> setResponseDto(T resultList, boolean ret, String msg) {
        return ResponseModel.<T>builder()
                .resultList(resultList)
                .result(ret)
                .message(msg)
                .build();
    }
}
