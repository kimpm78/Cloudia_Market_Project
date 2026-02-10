package com.cloudia.backend.CM_03_1001.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_03_1001.constants.CM031001MessageConstant;
import com.cloudia.backend.CM_03_1001.mapper.CM031001Mapper;
import com.cloudia.backend.CM_03_1001.model.CartRequest;
import com.cloudia.backend.CM_03_1001.model.Categories;
import com.cloudia.backend.CM_03_1001.model.CategoryDetails;
import com.cloudia.backend.CM_03_1001.model.CategoryGroupForCheckbox;
import com.cloudia.backend.CM_03_1001.model.CategoryItem;
import com.cloudia.backend.CM_03_1001.model.ProductDetails;
import com.cloudia.backend.CM_03_1001.model.ProductInfo;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_03_1001.service.CM031001Service;
import com.cloudia.backend.common.util.DateCalculator;
import com.cloudia.backend.constants.CMMessageConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM031001ServiceImpl implements CM031001Service {

    private final CM031001Mapper cm031001Mapper;
    private final DateCalculator dateCalculator;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<ProductInfo>>> getReservationProductList(List<String> categories) {
        try {
            List<ProductInfo> list = Optional.ofNullable(cm031001Mapper.selectReservationProductList(categories))
                .orElse(Collections.emptyList());
            applyReservationDeadlineStatus(list);
            return ResponseEntity.ok(setResponseDto(list, true, CM031001MessageConstant.SUCCESS_PRODUCT_FIND));
        } catch (Exception e) {
            log.error(CMMessageConstant.ERROR_DATABASE + " - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(setResponseDto(null, false, CMMessageConstant.ERROR_DATABASE));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<ProductInfo>>> getProductDetail(Long productId) {
        try {
            // 基本商品情報（単件）
            ProductInfo info = cm031001Mapper.selectProductDetail(productId);

            if (info == null) {
                log.info(CM031001MessageConstant.PRODUCT_FIND_BY_ID_COMPLETE, productId, 0);
                // 既存互換：空リスト＋成功メッセージ
                return ResponseEntity.ok(setResponseDto(Collections.emptyList(), true, CM031001MessageConstant.SUCCESS_PRODUCT_FIND));
            }

            // 詳細（サムネイル/説明/重量など）を取得し、空の場合のみ注入（上書き防止）
            ProductDetails details = cm031001Mapper.selectProductDetails(productId);
            if (details != null) {
                if ((info.getThumbnailUrl() == null || info.getThumbnailUrl().isBlank())
                        && details.getThumbnailUrl() != null && !details.getThumbnailUrl().isBlank()) {
                    info.setThumbnailUrl(details.getThumbnailUrl());
                }
                if ((info.getDescription() == null || info.getDescription().isBlank())
                        && details.getDescription() != null && !details.getDescription().isBlank()) {
                    info.setDescription(details.getDescription());
                }
                if (info.getWeight() == null && details.getWeight() != null) {
                    info.setWeight(details.getWeight());
                }
            }

            List<String> detailImages = cm031001Mapper.selectProductDetailImages(productId);
            info.setDetailImages(detailImages);

            applyReservationDeadlineStatus(Collections.singletonList(info));

            log.info(CM031001MessageConstant.PRODUCT_FIND_BY_ID_COMPLETE, productId, 1);
            return ResponseEntity.ok(setResponseDto(Collections.singletonList(info), true, CM031001MessageConstant.SUCCESS_PRODUCT_FIND));
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
            if (cm031001Mapper.findCartItem(cartRequest.getUserId(), cartRequest.getProductId()) != null) {
                cm031001Mapper.updateQuantity(
                    cartRequest.getUserId(),
                    cartRequest.getProductId(),
                    cartRequest.getQuantity()
                );
            } else {
                cm031001Mapper.insertCartItem(
                    cartRequest.getUserId(),
                    cartRequest.getProductId(),
                    cartRequest.getQuantity()
                );
            }
            return ResponseEntity.ok(setResponseDto(null, true, CM031001MessageConstant.SUCCESS_CART_ADD));
        } catch (Exception e) {
            log.error(CMMessageConstant.ERROR_DATABASE + " - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(setResponseDto(null, false, CM031001MessageConstant.FAIL_CART_ADD));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode() {
        try {
            List<Categories> categoryGroupCodeList = cm031001Mapper.findAllCategoryGroupCode();
            log.info(CM031001MessageConstant.CATEGORY_GROUP_FETCH_SUCCESS, categoryGroupCodeList == null ? 0 : categoryGroupCodeList.size());

            return ResponseEntity.ok(createResponseModel(categoryGroupCodeList, true, CM031001MessageConstant.CATEGORY_GROUP_FETCH_SUCCESS));
        } catch (DataAccessException dae) {
            log.error(CM031001MessageConstant.CATEGORY_GROUP_FETCH_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));
        } catch (NullPointerException npe) {
            log.error(CM031001MessageConstant.CATEGORY_GROUP_FETCH_NULL, npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.MESSAGE_NULL_NOT_ALLOWED));
        } catch (Exception e) {
            log.error(CM031001MessageConstant.CATEGORY_GROUP_FETCH_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CM031001MessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(List<String> categoryGroupCode) {
        try {
            List<CategoryDetails> CategoryDetailList = cm031001Mapper.findCategory(categoryGroupCode);
            log.info(CM031001MessageConstant.CATEGORY_DETAIL_FETCH_SUCCESS, CategoryDetailList == null ? 0 : CategoryDetailList.size());

            return ResponseEntity.ok(createResponseModel(CategoryDetailList, true, CM031001MessageConstant.CATEGORY_DETAIL_FETCH_SUCCESS));
        } catch (DataAccessException dae) {
            log.error(CM031001MessageConstant.CATEGORY_DETAIL_FETCH_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));
        } catch (NullPointerException npe) {
            log.error(CM031001MessageConstant.CATEGORY_DETAIL_FETCH_NULL, npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.MESSAGE_NULL_NOT_ALLOWED));
        } catch (Exception e) {
            log.error(CM031001MessageConstant.CATEGORY_DETAIL_FETCH_ERROR, e.getMessage(), e);
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

            return ResponseEntity.ok(createResponseModel(result, true, CM031001MessageConstant.CATEGORY_GROUP_FETCH_SUCCESS));
        } catch (Exception e) {
            log.error(CM031001MessageConstant.CATEGORY_GROUP_FETCH_ERROR, e.getMessage(), e);
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
            List<ProductInfo> list = Optional.ofNullable(cm031001Mapper.selectReservationProductList(categories))
                .orElse(Collections.emptyList());
            applyReservationDeadlineStatus(list);
            return ResponseEntity.ok(setResponseDto(list, true, CM031001MessageConstant.SUCCESS_PRODUCT_FIND));
        } catch (Exception e) {
            log.error(CMMessageConstant.ERROR_DATABASE + " - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(setResponseDto(null, false, CMMessageConstant.ERROR_DATABASE));
        }
    }

    private void applyReservationDeadlineStatus(List<ProductInfo> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        for (ProductInfo product : products) {
            if (product == null) {
                continue;
            }

            // 予約締切フラグ（ビジネスルール）
            product.setIsReservationClosed(product.getCodeValue() == 4);

            // 売り切れ判定
            Integer availableQty = product.getAvailableQty();
            if (product.getCodeValue() == 2 || product.getCodeValue() == 4) {
                product.setIsSoldOut(true);
            } else if (product.getCodeValue() == 3) {
                product.setIsSoldOut(false);
            } else {
                product.setIsSoldOut(availableQty == null || availableQty <= 0);
            }

            // 通常販売商品の配送予定日
            if (product.getReservationDeadline() == null || product.getReservationDeadline().isBlank()) {
                product.setEstimatedDeliveryDate(
                    dateCalculator.convertToYYMMDD(dateCalculator.tokyoTime(), 5)
                );
            }
        }
    }
}
