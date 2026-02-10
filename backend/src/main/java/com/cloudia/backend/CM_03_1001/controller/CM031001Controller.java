package com.cloudia.backend.CM_03_1001.controller;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.CM_03_1001.constants.CM031001MessageConstant;
import com.cloudia.backend.CM_03_1001.model.CartRequest;
import com.cloudia.backend.CM_03_1001.model.Categories;
import com.cloudia.backend.CM_03_1001.model.CategoryDetails;
import com.cloudia.backend.CM_03_1001.model.CategoryGroupForCheckbox;
import com.cloudia.backend.CM_03_1001.model.CategoryItem;
import com.cloudia.backend.CM_03_1001.model.ProductInfo;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_03_1001.service.CM031001Service;

import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/guest")
public class CM031001Controller {
    private final CM031001Service cm031001Service;

    /**
     * 新商品全体リスト取得
     *
     * @return 商品 リスト
     */
    @GetMapping("/pre-order")
    public ResponseEntity<ResponseModel<List<ProductInfo>>> getProductList(
        @RequestParam(name = "categories", required = false) List<String> categories
    ) {
        log.info(CM031001MessageConstant.PRODUCT_FIND_ALL_START);
        log.info(CM031001MessageConstant.LOG_CATEGORY_FILTER, categories);

        ResponseEntity<ResponseModel<List<ProductInfo>>> response = cm031001Service.getProductList(categories);

        if (response == null || response.getBody() == null) {
            log.warn(CM031001MessageConstant.FAIL_PRODUCT_LIST_FETCH_NULL);
            return ResponseEntity.internalServerError()
                .body(setResponseDto(null, false, CM031001MessageConstant.FAIL_PRODUCT_LIST_FETCH_NULL));
        }

        List<ProductInfo> products = response.getBody().getResultList();

        if (products == null) {
            log.warn(CM031001MessageConstant.FAIL_PRODUCT_LIST_EMPTY);
            return ResponseEntity.ok(setResponseDto(Collections.emptyList(), true, CM031001MessageConstant.FAIL_PRODUCT_LIST_EMPTY));
        }
        // 予約商品（プレオーダー）のみをフィルタリング
        log.info(CM031001MessageConstant.PRODUCT_FIND_ALL_COMPLETE, products.size());
        return ResponseEntity.ok(setResponseDto(products, true, CM031001MessageConstant.SUCCESS_PRODUCT_FIND));
    }

    /**
     * 商品詳細取得
     *
     * @param detailId 商品ID
     * @return 商品詳細情報
     */
    @GetMapping("/pre-order/{detailId}")
    public ResponseEntity<ResponseModel<ProductInfo>> getProductDetail(@PathVariable Long detailId) {
        log.info(CM031001MessageConstant.PRODUCT_FIND_BY_ID_START, detailId);
        try {
            ResponseEntity<ResponseModel<List<ProductInfo>>> svc = cm031001Service.getProductDetail(detailId);

            if (svc == null || svc.getBody() == null) {
                log.warn(CM031001MessageConstant.WARN_PRODUCT_DETAIL_RESPONSE_NULL, detailId);
                return ResponseEntity.internalServerError()
                    .body(setResponseDto(null, false, CM031001MessageConstant.ERROR_INTERNAL_SERVER));
            }

            List<ProductInfo> list = svc.getBody().getResultList();
            ProductInfo product = (list != null && !list.isEmpty()) ? list.get(0) : null;

            if (product == null) {
                log.info(CM031001MessageConstant.PRODUCT_FIND_BY_ID_COMPLETE, detailId, 0);
                return ResponseEntity.ok(setResponseDto(null, false, CM031001MessageConstant.FAIL_PRODUCT_NOT_FOUND));
            }

            log.info(CM031001MessageConstant.PRODUCT_FIND_BY_ID_COMPLETE, detailId, 1);
            return ResponseEntity.ok(setResponseDto(product, true, CM031001MessageConstant.SUCCESS_PRODUCT_FIND));
        } catch (Exception e) {
            log.error("商品詳細取得中に例外が発生しました: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(setResponseDto(null, false, CM031001MessageConstant.ERROR_INTERNAL_SERVER));
        }
    }
    
    /**
     * カテゴリグループコード全体リスト取得
     *
     * @return カテゴリグループコード全体リスト
     */
    @GetMapping("/pre-order/categoryGroupCode")
    public ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode() {
        log.info(CM031001MessageConstant.CATEGORY_GROUP_FETCH_START);
        ResponseEntity<ResponseModel<List<Categories>>> response = cm031001Service.findAllCategoryGroupCode();
        log.info(CM031001MessageConstant.CATEGORY_GROUP_FETCH_SUCCESS);
        return response;
    }

    /**
     * 選択されたカテゴリグループの下位カテゴリ情報取得
     *
     * @param categoryGroupCodes カテゴリグループコードリスト
     * @return 下位カテゴリ情報
     */
    @PostMapping("/pre-order/findCategory")
    public ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(
            @RequestBody List<String> categoryGroupCodes) {
        log.info(CM031001MessageConstant.CATEGORY_DETAIL_FETCH_START, categoryGroupCodes);
        ResponseEntity<ResponseModel<List<CategoryDetails>>> response = cm031001Service
                .findCategory(categoryGroupCodes);
        log.info(CM031001MessageConstant.CATEGORY_DETAIL_FETCH_SUCCESS);
        return response;
    }
    
    /**
     * チェックボックス用カテゴリグループ + 上位カテゴリリスト API
     */
    @GetMapping("/pre-order/categoryGroupForCheckbox")
    public ResponseEntity<ResponseModel<List<CategoryGroupForCheckbox>>> getCategoryGroupForCheckbox() {
        log.info(CM031001MessageConstant.LOG_CATEGORY_GROUP_FOR_CHECKBOX_START);

        List<Categories> rawGroups = cm031001Service.findAllCategoryGroupCode().getBody().getResultList();

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

        return ResponseEntity.ok(setResponseDto(result, true, CM031001MessageConstant.SUCCESS_CATEGORY_GROUP_FOR_CHECKBOX));
    }

    /**
     * カートに商品追加
     *
     * @param cartRequest カート追加リクエスト
     * @param bindingResult バリデーション結果
     * @return カート追加結果
     */
    @PostMapping("/pre-order/cart")
    public ResponseEntity<ResponseModel<Void>> addToCart(@RequestBody @Valid CartRequest cartRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("\r\n "));
            log.warn(CM031001MessageConstant.FAIL_CART_ADD, errorMessage);
            return ResponseEntity.badRequest().body(setResponseDto(null, false, errorMessage));
        }

        log.info(CM031001MessageConstant.SUCCESS_CART_ADD);
        cm031001Service.addToCart(cartRequest);
        log.info(CM031001MessageConstant.SUCCESS_CART_UPDATE);
        return ResponseEntity.ok(setResponseDto(null, true, CM031001MessageConstant.SUCCESS_CART_ADD));
    }

    /**
     * 共通 応答モデル設定
     *
     * @param resultList 結果データ
     * @param ret        成功 有無
     * @param msg        メッセージ
     * @return           共通応答モデル
     */
    private <T> ResponseModel<T> setResponseDto(T resultList, boolean ret, String msg) {
        return ResponseModel.<T>builder()
                .resultList(resultList)
                .result(ret)
                .message(msg)
                .build();
    }
}
