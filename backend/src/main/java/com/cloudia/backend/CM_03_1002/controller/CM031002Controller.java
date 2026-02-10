package com.cloudia.backend.CM_03_1002.controller;

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

import com.cloudia.backend.CM_03_1002.constants.CM031002MessageConstant;
import com.cloudia.backend.CM_03_1002.model.CartRequest;
import com.cloudia.backend.CM_03_1002.model.Categories;
import com.cloudia.backend.CM_03_1002.model.CategoryDetails;
import com.cloudia.backend.CM_03_1002.model.CategoryGroupForCheckbox;
import com.cloudia.backend.CM_03_1002.model.CategoryItem;
import com.cloudia.backend.CM_03_1002.model.ProductInfo;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_03_1002.service.CM031002Service;

import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/guest")
public class CM031002Controller {
    private final CM031002Service cm031002Service;

    // 商品一覧
    /**
     * 新商品一覧の取得
     *
     * @return 商品リスト
     */
    @GetMapping("/characters")
    public ResponseEntity<ResponseModel<List<ProductInfo>>> getProductList(
        @RequestParam(name = "categories", required = false) List<String> categories
    ) {
        log.info(CM031002MessageConstant.PRODUCT_FIND_ALL_START);
        log.info("カテゴリーフィルター: {}", categories);

        ResponseEntity<ResponseModel<List<ProductInfo>>> response = cm031002Service.getProductList(categories);

        if (response == null || response.getBody() == null) {
            log.warn(CM031002MessageConstant.FAIL_PRODUCT_LIST_FETCH_NULL);
            return ResponseEntity.internalServerError()
                .body(setResponseDto(null, false, CM031002MessageConstant.FAIL_PRODUCT_LIST_FETCH_NULL));
        }

        List<ProductInfo> products = response.getBody().getResultList();

        if (products == null) {
            log.warn(CM031002MessageConstant.FAIL_PRODUCT_LIST_EMPTY);
            return ResponseEntity.ok(setResponseDto(Collections.emptyList(), true, CM031002MessageConstant.FAIL_PRODUCT_LIST_EMPTY));
        }

        log.info(CM031002MessageConstant.PRODUCT_FIND_ALL_COMPLETE, products.size());
        return ResponseEntity.ok(setResponseDto(products, true, CM031002MessageConstant.SUCCESS_PRODUCT_FIND));
    }

    // 商品詳細
    /**
     * 商品詳細取得
     *
     * @param detailId 商品ID
     * @return 商品詳細情報
     */
    @GetMapping("/characters/{detailId}")
    public ResponseEntity<ResponseModel<ProductInfo>> getProductDetail(@PathVariable Long detailId) {
        log.info(CM031002MessageConstant.PRODUCT_FIND_BY_ID_START, detailId);
        List<ProductInfo> list = cm031002Service.getProductDetail(detailId).getBody().getResultList();
        ProductInfo product = (list != null && !list.isEmpty()) ? list.get(0) : null;
        log.info(CM031002MessageConstant.PRODUCT_FIND_BY_ID_COMPLETE, detailId, product != null ? 1 : 0);
        return ResponseEntity.ok(setResponseDto(product, true, CM031002MessageConstant.SUCCESS_PRODUCT_FIND));
    }
    
    /**
     * カテゴリグループコード一覧取得
     *
     * @return カテゴリグループコード一覧
     */
    @GetMapping("/characters/categoryGroupCode")
    public ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode() {
        log.info(CM031002MessageConstant.CATEGORY_GROUP_FETCH_START);
        ResponseEntity<ResponseModel<List<Categories>>> response = cm031002Service.findAllCategoryGroupCode();
        log.info(CM031002MessageConstant.CATEGORY_GROUP_FETCH_SUCCESS);
        return response;
    }

    /**
     * 選択されたカテゴリグループの下位カテゴリ情報取得
     *
     * @param categoryGroupCodes カテゴリグループコード一覧
     * @return 下位カテゴリ情報
     */
    @PostMapping("/characters/findCategory")
    public ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(
            @RequestBody List<String> categoryGroupCodes) {
        log.info(CM031002MessageConstant.CATEGORY_DETAIL_FETCH_START, categoryGroupCodes);
        ResponseEntity<ResponseModel<List<CategoryDetails>>> response = cm031002Service
                .findCategory(categoryGroupCodes);
        log.info(CM031002MessageConstant.CATEGORY_DETAIL_FETCH_SUCCESS);
        return response;
    }
    
    /**
     * チェックボックス用：カテゴリグループ＋詳細一覧API
     */
    @GetMapping("/characters/categoryGroupForCheckbox")
    public ResponseEntity<ResponseModel<List<CategoryGroupForCheckbox>>> getCategoryGroupForCheckbox() {
        log.info("チェックボックス用カテゴリグループ呼び出し開始");

        List<Categories> rawGroups = cm031002Service.findAllCategoryGroupCode().getBody().getResultList();

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

        return ResponseEntity.ok(setResponseDto(result, true, "カテゴリグループ＋項目一覧の返却が完了しました"));
    }

    
    // カート追加
    /**
     * カートに商品を追加
     *
     * @param cartRequest カート追加リクエスト
     * @param bindingResult バリデーション結果
     * @return カート追加結果
     */
    @PostMapping("/characters/cart")
    public ResponseEntity<ResponseModel<Void>> addToCart(@RequestBody @Valid CartRequest cartRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("\r\n "));
            log.warn(CM031002MessageConstant.FAIL_CART_ADD, errorMessage);
            return ResponseEntity.badRequest().body(setResponseDto(null, false, errorMessage));
        }

        log.info(CM031002MessageConstant.SUCCESS_CART_ADD);
        cm031002Service.addToCart(cartRequest);
        log.info(CM031002MessageConstant.SUCCESS_CART_UPDATE);
        return ResponseEntity.ok(setResponseDto(null, true, CM031002MessageConstant.SUCCESS_CART_ADD));
    }

    /**
     * 共通レスポンスフォーマット設定
     *
     * @param resultList 結果データ
     * @param ret        成功可否
     * @param msg        メッセージ
     * @return 共通レスポンスモデル
     */
    private <T> ResponseModel<T> setResponseDto(T resultList, boolean ret, String msg) {
        return ResponseModel.<T>builder()
                .resultList(resultList)
                .result(ret)
                .message(msg)
                .build();
    }
}
