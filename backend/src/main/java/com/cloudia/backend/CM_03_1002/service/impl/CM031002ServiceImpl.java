package com.cloudia.backend.CM_03_1002.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_03_1002.constants.CM031002MessageConstant;
import com.cloudia.backend.CM_03_1002.mapper.CM031002Mapper;
import com.cloudia.backend.CM_03_1002.model.CartRequest;
import com.cloudia.backend.CM_03_1002.model.Categories;
import com.cloudia.backend.CM_03_1002.model.CategoryDetails;
import com.cloudia.backend.CM_03_1002.model.CategoryGroupForCheckbox;
import com.cloudia.backend.CM_03_1002.model.CategoryItem;
import com.cloudia.backend.CM_03_1002.model.ProductInfo;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_03_1002.service.CM031002Service;
import com.cloudia.backend.constants.CMMessageConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM031002ServiceImpl implements CM031002Service {

    private final CM031002Mapper cm031002Mapper;



    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<ProductInfo>>> getProductDetail(Long productId) {
        try {
            List<ProductInfo> list = Optional.ofNullable(cm031002Mapper.selectProductDetail(productId))
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
            log.info(CM031002MessageConstant.PRODUCT_FIND_BY_ID_COMPLETE, productId, list.size());
            return ResponseEntity.ok(setResponseDto(list, true, CM031002MessageConstant.SUCCESS_PRODUCT_FIND));
        } catch (Exception e) {
            log.error(CMMessageConstant.ERROR_DATABASE + " - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(setResponseDto(null, false, CMMessageConstant.ERROR_DATABASE));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Void>> addToCart(CartRequest cartRequest) {
        try {
            if (cm031002Mapper.findCartItem(cartRequest.getUserId(), cartRequest.getProductId()) != null) {
                cm031002Mapper.updateQuantity(
                    cartRequest.getUserId(),
                    cartRequest.getProductId(),
                    cartRequest.getQuantity()
                );
            } else {
                cm031002Mapper.insertCartItem(
                    cartRequest.getUserId(),
                    cartRequest.getProductId(),
                    cartRequest.getQuantity()
                );
            }
            return ResponseEntity.ok(setResponseDto(null, true, CM031002MessageConstant.SUCCESS_CART_ADD));
        } catch (Exception e) {
            log.error(CMMessageConstant.ERROR_DATABASE + " - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(setResponseDto(null, false, CM031002MessageConstant.FAIL_CART_ADD));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode() {
        try {
            List<Categories> categoryGroupCodeList = cm031002Mapper.findAllCategoryGroupCode();
            log.info(CM031002MessageConstant.CATEGORY_GROUP_FETCH_SUCCESS, categoryGroupCodeList == null ? 0 : categoryGroupCodeList.size());

            return ResponseEntity.ok(createResponseModel(categoryGroupCodeList, true, CM031002MessageConstant.CATEGORY_GROUP_FETCH_SUCCESS));
        } catch (DataAccessException dae) {
            log.error(CM031002MessageConstant.CATEGORY_GROUP_FETCH_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));
        } catch (NullPointerException npe) {
            log.error(CM031002MessageConstant.CATEGORY_GROUP_FETCH_NULL, npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.MESSAGE_NULL_NOT_ALLOWED));
        } catch (Exception e) {
            log.error(CM031002MessageConstant.CATEGORY_GROUP_FETCH_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(List<String> categoryGroupCode) {
        try {
            List<CategoryDetails> CategoryDetailList = cm031002Mapper.findCategory(categoryGroupCode);
            log.info(CM031002MessageConstant.CATEGORY_DETAIL_FETCH_SUCCESS, CategoryDetailList == null ? 0 : CategoryDetailList.size());

            return ResponseEntity.ok(createResponseModel(CategoryDetailList, true, CM031002MessageConstant.CATEGORY_DETAIL_FETCH_SUCCESS));
        } catch (DataAccessException dae) {
            log.error(CM031002MessageConstant.CATEGORY_DETAIL_FETCH_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));
        } catch (NullPointerException npe) {
            log.error(CM031002MessageConstant.CATEGORY_DETAIL_FETCH_NULL, npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.MESSAGE_NULL_NOT_ALLOWED));
        } catch (Exception e) {
            log.error(CM031002MessageConstant.CATEGORY_DETAIL_FETCH_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<CategoryGroupForCheckbox>>> findAllCategoryGroupForCheckbox() {
        try {
            List<Categories> rawGroups = findAllCategoryGroupCode().getBody().getResultList();

            List<CategoryGroupForCheckbox> result = rawGroups.stream().map(group ->
                CategoryGroupForCheckbox.builder()
                    .groupCode(group.getCategoryGroupCode())
                    .groupName(group.getCategoryGroupName())
                    .categories(
                        group.getDetails().stream().map(detail ->
                            CategoryItem.builder()
                                .code(detail.getCategoryCode())
                                .name(detail.getCategoryName())
                                .build()
                        ).toList()
                    )
                    .build()
            ).toList();

            return ResponseEntity.ok(createResponseModel(result, true, CM031002MessageConstant.CATEGORY_GROUP_FETCH_SUCCESS));
        } catch (Exception e) {
            log.error(CM031002MessageConstant.CATEGORY_GROUP_FETCH_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    private <T> ResponseModel<T> setResponseDto(T resultList, boolean ret, String msg) {
        return ResponseModel.<T>builder()
            .resultList(resultList)
            .result(ret)
            .message(msg)
            .build();
    }

    private <T> ResponseModel<T> createResponseModel(T data, boolean result, String message) {
        return ResponseModel.<T>builder()
                .resultList(data)
                .result(result)
                .message(message)
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<ProductInfo>>> getProductList(List<String> categories) {
        try {
            List<ProductInfo> list = Optional.ofNullable(cm031002Mapper.selectNewProductList(categories))
                .orElse(Collections.emptyList());
            return ResponseEntity.ok(setResponseDto(list, true, CM031002MessageConstant.SUCCESS_PRODUCT_FIND));
        } catch (Exception e) {
            log.error(CMMessageConstant.ERROR_DATABASE + " - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(setResponseDto(null, false, CMMessageConstant.ERROR_DATABASE));
        }
    }
}