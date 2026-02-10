package com.cloudia.backend.CM_03_1000.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cloudia.backend.CM_03_1000.constants.CM031000MessageConstant;
import com.cloudia.backend.CM_03_1000.model.CartRequest;
import com.cloudia.backend.CM_03_1000.model.Categories;
import com.cloudia.backend.CM_03_1000.model.CategoryDetails;
import com.cloudia.backend.CM_03_1000.model.CategoryGroupForCheckbox;
import com.cloudia.backend.CM_03_1000.model.CategoryItem;
import com.cloudia.backend.CM_03_1000.model.ProductInfo;
import com.cloudia.backend.CM_03_1000.service.CM031000Service;
import com.cloudia.backend.common.model.ResponseModel;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/guest")
public class CM031000Controller {
    private final CM031000Service cm031000Service;

    // 商品一覧
    /**
     * 新商品一覧の全件取得
     *
     * @return 商品一覧
     */
    @GetMapping("/new-product")
    public ResponseEntity<ResponseModel<List<ProductInfo>>> getProductList(
        @RequestParam(name = "categories", required = false) List<String> categories
    ) {
        log.info(CM031000MessageConstant.PRODUCT_FIND_ALL_START);
        log.info("カテゴリーフィルター: {}", categories);

        ResponseEntity<ResponseModel<List<ProductInfo>>> response = cm031000Service.getProductList(categories);

        if (response == null || response.getBody() == null) {
            log.warn(CM031000MessageConstant.FAIL_PRODUCT_LIST_FETCH_NULL);
            return ResponseEntity.internalServerError()
                .body(setResponseDto(null, false, CM031000MessageConstant.FAIL_PRODUCT_LIST_FETCH_NULL));
        }

        List<ProductInfo> products = response.getBody().getResultList();
        if (products == null) {
            log.warn(CM031000MessageConstant.FAIL_PRODUCT_LIST_EMPTY);
            return ResponseEntity.ok(setResponseDto(Collections.emptyList(), true, CM031000MessageConstant.FAIL_PRODUCT_LIST_EMPTY));
        }

        log.info(CM031000MessageConstant.PRODUCT_FIND_ALL_COMPLETE, products.size());
        return ResponseEntity.ok(setResponseDto(products, true, CM031000MessageConstant.SUCCESS_PRODUCT_FIND));
    }

    // 商品詳細
    /**
     * 商品詳細取得
     *
     * @param detailId 商品ID
     * @return 商品詳細情報
     */
    @GetMapping("/new-product/{detailId}")
    public ResponseEntity<ResponseModel<ProductInfo>> getProductDetail(@PathVariable Long detailId) {
        log.info(CM031000MessageConstant.PRODUCT_FIND_BY_ID_START, detailId);
        List<ProductInfo> list = cm031000Service.getProductDetail(detailId).getBody().getResultList();
        ProductInfo product = (list != null && !list.isEmpty()) ? list.get(0) : null;
        log.info(CM031000MessageConstant.PRODUCT_FIND_BY_ID_COMPLETE, detailId, product != null ? 1 : 0);
        return ResponseEntity.ok(setResponseDto(product, true, CM031000MessageConstant.SUCCESS_PRODUCT_FIND));
    }
    
    /**
     * カテゴリーグループコードの全件取得
     *
     * @return カテゴリーグループコード一覧
     */
    @GetMapping("/product/categoryGroupCode")
    public ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode() {
        log.info(CM031000MessageConstant.CATEGORY_GROUP_FETCH_START);
        ResponseEntity<ResponseModel<List<Categories>>> response = cm031000Service.findAllCategoryGroupCode();
        log.info(CM031000MessageConstant.CATEGORY_GROUP_FETCH_SUCCESS);
        return response;
    }

    /**
     * 選択したカテゴリーグループの下位カテゴリー情報取得
     *
     * @param categoryGroupCodes カテゴリーグループコード一覧
     * @return 下位カテゴリー情報
     */
    @PostMapping("/product/findCategory")
    public ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(
            @RequestBody List<String> categoryGroupCodes) {
        log.info(CM031000MessageConstant.CATEGORY_DETAIL_FETCH_START, categoryGroupCodes);
        ResponseEntity<ResponseModel<List<CategoryDetails>>> response = cm031000Service
                .findCategory(categoryGroupCodes);
        log.info(CM031000MessageConstant.CATEGORY_DETAIL_FETCH_SUCCESS);
        return response;
    }
    
    /**
     * チェックボックス用：カテゴリーグループ＋詳細一覧 API
     */
    @GetMapping("/product/categoryGroupForCheckbox")
    public ResponseEntity<ResponseModel<List<CategoryGroupForCheckbox>>> getCategoryGroupForCheckbox() {
        log.info("チェックボックス用カテゴリーグループ呼び出し開始");

        List<Categories> rawGroups = cm031000Service.findAllCategoryGroupCode().getBody().getResultList();

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

        return ResponseEntity.ok(setResponseDto(result, true, CM031000MessageConstant.SUCCESS_CATEGORY_GROUP_FOR_CHECKBOX));
    }

    
    // カート追加
    /**
     * カートに商品を追加（予約商品は必ずカートへ追加）
     *
     * @param cartRequest カート追加リクエスト
     * @param bindingResult バリデーション結果
     * @return カート追加結果
     */
    @PostMapping("/cart")
    public ResponseEntity<ResponseModel<Void>> addToCart(
            @RequestBody @Valid CartRequest cartRequest,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining("\r\n "));
            log.warn(CM031000MessageConstant.FAIL_CART_ADD, errorMessage);
            return ResponseEntity.badRequest().body(setResponseDto(null, false, errorMessage));
        }

        // 予約商品の判定
        if (cartRequest.isReservation()) {
            log.info("予約商品のリクエスト - 必ずカートへ追加処理");
            cm031000Service.addToCart(cartRequest);
            return ResponseEntity.ok(setResponseDto(null, true, CM031000MessageConstant.SUCCESS_CART_ADD));
        }

        // 通常商品の場合は既存ロジックをそのまま実行
        log.info(CM031000MessageConstant.SUCCESS_CART_ADD);
        cm031000Service.addToCart(cartRequest);
        log.info(CM031000MessageConstant.SUCCESS_CART_UPDATE);

        return ResponseEntity.ok(setResponseDto(null, true, CM031000MessageConstant.SUCCESS_CART_UPDATE));
    }

    /**
     * 画像アップロード
     *
     * @param file アップロードするファイル
     * @return アップロード結果
     */
    @PostMapping("/product/image/upload")
    public ResponseEntity<ResponseModel<String>> postImageUpload(@RequestParam("file") MultipartFile file) {
        return cm031000Service.imageUpload(file);
    }

    /**
     * 共通レスポンスフォーマット設定
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
