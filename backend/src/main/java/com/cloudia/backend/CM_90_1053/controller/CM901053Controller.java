package com.cloudia.backend.CM_90_1053.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.CM_90_1053.model.Stocks;
import com.cloudia.backend.CM_90_1053.service.CM901053Service;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.util.ResponseHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController("CM_90_1053")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin")
public class CM901053Controller {
    // Service 정의
    private final CM901053Service cm901053Service;

    /**
     * 재고 현황 조회
     * 
     * @return 재고 현황 리스트
     */
    @GetMapping("/stocks/status")
    public ResponseEntity<ResponseModel<List<Stocks>>> findAllProductCode() {
        List<Stocks> response = cm901053Service.findAllStockStatus();
        return ResponseEntity.ok(ResponseHelper.success(response, "조회 성공"));
    }

    /**
     * 선택 된 상품 코드 / 상품명의 재고 현황 정보 조회
     * 
     * @param searchType 검색 타입 (1: 상품 코드 2: 상품 명)
     * @param searchTerm 검색어
     * @return 재고 현황 정보
     */
    @GetMapping("/stocks/findstatus")
    public ResponseEntity<ResponseModel<List<Stocks>>> findByStockStatus(@RequestParam String searchType,
            @RequestParam String searchTerm) {
        List<Stocks> response = cm901053Service.findByStockStatus(searchType, searchTerm);
        return ResponseEntity.ok(ResponseHelper.success(response, "조회 성공"));
    }
}
