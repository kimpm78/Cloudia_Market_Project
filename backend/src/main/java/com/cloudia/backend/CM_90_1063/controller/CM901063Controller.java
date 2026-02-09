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
    // Service 정의
    private final CM901063Service cm901063Service;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 상품 코드 전체 리스트 조회
     * 
     * @return 상품 코드 전체 리스트
     */
    @GetMapping("/productCode/all")
    public ResponseEntity<ResponseModel<List<ProductCode>>> findAllProductCode() {
        List<ProductCode> response = cm901063Service.findAllProductCode();
        return ResponseEntity.ok(ResponseHelper.success(response, "조회 성공"));
    }

    /**
     * 특정 상품 코드 가격 조회
     * 
     * @param searchCode 검색 상품 코드
     * @return 상품 코드 리스트
     */
    @GetMapping("/productCode/{searchCode}")
    public ResponseEntity<ResponseModel<Optional<Stock>>> getStock(@PathVariable String searchCode) {
        Optional<Stock> product = cm901063Service.getStockByCode(searchCode);
        return ResponseEntity.ok(ResponseHelper.success(product, "조회 성공"));
    }

    /**
     * 입/출고 일람 전체 조회
     * 
     * @return 입/출고 일람 전체 리스트
     */
    @GetMapping("/stocks/all")
    public ResponseEntity<ResponseModel<List<StockInfo>>> findAllStocks() {
        List<StockInfo> stocks = cm901063Service.findAllStocks();
        return ResponseEntity.ok(ResponseHelper.success(stocks, "조회 성공"));
    }

    /**
     * 선택 된 상품 코드 / 상품명의 상품 가격 정보 조회
     * 
     * @param searchType 검색 타입 (1: 상품 코드 2: 상품 명)
     * @param searchTerm 검색어
     * @return 상품 정보
     */
    @GetMapping("/stocks/findStocks")
    public ResponseEntity<ResponseModel<List<StockInfo>>> findByStocks(@RequestParam String searchType,
            @RequestParam String searchTerm) {
        List<StockInfo> stock = cm901063Service.findByStocks(searchType, searchTerm);
        return ResponseEntity.ok(ResponseHelper.success(stock, "조회 성공"));
    }

    /**
     * 재고 입/출고 등록
     *
     * @param entity 등록 할 재고 정보 엔티티
     * 
     * @return 등록 성공 여부
     **/
    @PutMapping("/stocks")
    public ResponseEntity<ResponseModel<Integer>> stockUpsert(@Valid @RequestBody ProductCode entity,
            BindingResult bindingResult, HttpServletRequest request) {
        String memberNumber = jwtTokenProvider.getMemberNoFromToken(jwtTokenProvider.resolveToken(request));
        Integer productOpt = cm901063Service.stockUpsert(entity, memberNumber);
        return ResponseEntity.ok(ResponseHelper.success(productOpt, "등록 성공"));
    }
}
