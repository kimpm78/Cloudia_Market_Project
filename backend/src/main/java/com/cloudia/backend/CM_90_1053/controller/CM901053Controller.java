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
    private final CM901053Service cm901053Service;

    /**
     * 在庫状況取得
     * 
     * @return 在庫状況リスト
     */
    @GetMapping("/stocks/status")
    public ResponseEntity<ResponseModel<List<Stocks>>> findAllProductCode() {
        List<Stocks> response = cm901053Service.findAllStockStatus();
        return ResponseEntity.ok(ResponseHelper.success(response, "取得成功"));
    }

    /**
     * 選択された商品コード／商品名の在庫状況情報取得
     * 
     * @param searchType 検索タイプ（1: 商品コード 2: 商品名）
     * @param searchTerm 検索キーワード
     * @return 在庫状況情報
     */
    @GetMapping("/stocks/findstatus")
    public ResponseEntity<ResponseModel<List<Stocks>>> findByStockStatus(@RequestParam String searchType,
            @RequestParam String searchTerm) {
        List<Stocks> response = cm901053Service.findByStockStatus(searchType, searchTerm);
        return ResponseEntity.ok(ResponseHelper.success(response, "取得成功"));
    }
}
