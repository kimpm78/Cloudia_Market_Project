package com.cloudia.backend.CM_90_1063.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.CM_90_1063.model.ProductCode;
import com.cloudia.backend.CM_90_1063.model.Stock;
import com.cloudia.backend.CM_90_1063.model.StockInfo;
import com.cloudia.backend.CM_90_1063.service.CM901063Service;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.util.ResponseHelper;
import com.cloudia.backend.config.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin")
public class CM901063Controller {
    private final CM901063Service cm901063Service;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 商品コード全件一覧取得
     *
     * @return 商品コード全件一覧
     */
    @GetMapping("/productCode/all")
    public ResponseEntity<ResponseModel<List<ProductCode>>> findAllProductCode() {
        List<ProductCode> response = cm901063Service.findAllProductCode();
        return ResponseEntity.ok(ResponseHelper.success(response, "取得成功"));
    }

    /**
     * 指定した商品コードの価格取得
     *
     * @param searchCode 検索商品コード
     * @return 商品コード情報
     */
    @GetMapping("/productCode/{searchCode}")
    public ResponseEntity<ResponseModel<Optional<Stock>>> getStock(@PathVariable String searchCode) {
        Optional<Stock> product = cm901063Service.getStockByCode(searchCode);
        return ResponseEntity.ok(ResponseHelper.success(product, "取得成功"));
    }

    /**
     * 入出庫一覧の全件取得
     *
     * @return 入出庫一覧（全件）
     */
    @GetMapping("/stocks/all")
    public ResponseEntity<ResponseModel<List<StockInfo>>> findAllStocks() {
        List<StockInfo> stocks = cm901063Service.findAllStocks();
        return ResponseEntity.ok(ResponseHelper.success(stocks, "取得成功"));
    }

    /**
     * 選択した商品コード／商品名で在庫・価格情報を検索
     *
     * @param searchType 検索種別（1: 商品コード / 2: 商品名）
     * @param searchTerm 検索キーワード
     * @return 商品情報
     */
    @GetMapping("/stocks/findStocks")
    public ResponseEntity<ResponseModel<List<StockInfo>>> findByStocks(@RequestParam String searchType,
            @RequestParam String searchTerm) {
        List<StockInfo> stock = cm901063Service.findByStocks(searchType, searchTerm);
        return ResponseEntity.ok(ResponseHelper.success(stock, "取得成功"));
    }

    /**
     * 在庫の入出庫登録
     *
     * @param entity 登録する在庫情報エンティティ
     * @return 登録結果
     */
    @PutMapping("/stocks")
    public ResponseEntity<ResponseModel<Integer>> stockUpsert(@Valid @RequestBody ProductCode entity,
            BindingResult bindingResult, HttpServletRequest request) {
        String memberNumber = jwtTokenProvider.getMemberNoFromToken(jwtTokenProvider.resolveToken(request));
        Integer productOpt = cm901063Service.stockUpsert(entity, memberNumber);
        return ResponseEntity.ok(ResponseHelper.success(productOpt, "登録成功"));
    }
}
