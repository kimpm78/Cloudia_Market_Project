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
import com.cloudia.backend.CM_90_1060.model.ResponseProducts;
import com.cloudia.backend.CM_90_1060.model.Stock;
import com.cloudia.backend.CM_90_1060.service.CM901060Service;
import com.cloudia.backend.common.model.ResponseModel;
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
    private final CM901060Service cm901060Service;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 商品全件リスト取得
     * 
     * @return 商品全件リスト
     */
    @GetMapping("/product/findAll")
    public ResponseEntity<ResponseModel<List<ResponseProducts>>> getfindAllProduct() {
        return cm901060Service.findAllProduct();
    }

    /**
     * 特定商品リスト取得
     * 
     * @param searchTerm キーワード
     * @param searchType 種別 (1:商品コード, 2:商品名)
     * @return 商品リスト
     */
    @GetMapping("/product/findProduct")
    public ResponseEntity<ResponseModel<List<ResponseProducts>>> getFindProduct(@RequestParam String searchTerm,
            @RequestParam int searchType) {
        return cm901060Service.getFindProduct(searchTerm, searchType);
    }

    /**
     * 特定商品取得
     * 
     * @param productId 商品コード
     * @return 特定商品
     */
    @GetMapping("/product/findByProductCode")
    public ResponseEntity<ResponseModel<ProductUpt>> findByProductCode(@RequestParam int productId) {
        return cm901060Service.findByProductCode(productId);
    }

    /**
     * 商品削除
     * 
     * @param productIds 削除対象IDリスト
     * @return 削除結果
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
     * カテゴリグループコード全件リスト取得
     * 
     * @return カテゴリグループコード全件リスト
     */
    @GetMapping("/product/categoryGroupCode")
    public ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode() {
        log.info("カテゴリグループコード一覧取得 Start");
        ResponseEntity<ResponseModel<List<Categories>>> response = cm901060Service.findAllCategoryGroupCode();
        log.info("カテゴリグループコード一覧取得 End");
        return response;
    }

    /**
     * 登録可能な在庫リスト取得
     * 
     * @return 在庫リスト
     */
    @GetMapping("/product/stockCode")
    public ResponseEntity<ResponseModel<List<Stock>>> findAllStockCode() {
        log.info("在庫コード一覧取得 Start");
        ResponseEntity<ResponseModel<List<Stock>>> response = cm901060Service.findAllStockCode();
        log.info("在庫コード一覧取得 End");
        return response;
    }

    /**
     * 選択されたカテゴリグループの下位カテゴリ情報取得
     * 
     * @param categoryGroupCodes カテゴリグループコード
     * @return 下位カテゴリ情報
     */
    @PostMapping("/product/findCategory")
    public ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(
            @RequestBody List<String> categoryGroupCodes) {
        log.info("特定下位カテゴリ情報取得 Start");
        ResponseEntity<ResponseModel<List<CategoryDetails>>> response = cm901060Service
                .findCategory(categoryGroupCodes);
        log.info("特定下位カテゴリ情報取得 End");
        return response;
    }

    /**
     * 商品登録
     * 
     * @param entity 登録する商品情報
     * @return 登録結果
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
     * 商品更新
     * 
     * @param entity 更新する商品情報
     * @return 更新結果
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
     * 画像登録
     * 
     * @param file 登録する画像ファイル
     * @return 登録結果
     */
    @PostMapping("/product/image/upload")
    public ResponseEntity<ResponseModel<String>> postImageUpload(@RequestBody MultipartFile file) {

        return cm901060Service.imageUpload(file);
    }
}
