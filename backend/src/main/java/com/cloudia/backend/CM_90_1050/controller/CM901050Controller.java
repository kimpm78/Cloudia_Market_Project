package com.cloudia.backend.CM_90_1050.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.CM_90_1050.model.ResultDto;
import com.cloudia.backend.CM_90_1050.model.SearchRequestDto;
import com.cloudia.backend.CM_90_1050.service.CM901050Service;
import com.cloudia.backend.common.util.ResponseHelper;
import com.cloudia.backend.common.model.ResponseModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/settlement/sales")
public class CM901050Controller {
    private final CM901050Service cm901050Service;

    /**
     * 売上情報一覧取得
     *
     * @return 売上情報の全件リスト
     */

    @GetMapping("/findAll")
    public ResponseEntity<ResponseModel<List<ResultDto>>> findByAllSales() {
        List<ResultDto> result = cm901050Service.findByAllSales();
        return ResponseEntity.ok(ResponseHelper.success(result, "照会成功"));
    }

    /**
     * 売上情報一覧取得
     *
     * @param searchData 検索条件
     * @return 売上情報の全件リスト
     */
    @GetMapping("/findSales")
    public ResponseEntity<ResponseModel<List<ResultDto>>> getFindSales(SearchRequestDto searchRequest) {
        List<ResultDto> result = cm901050Service.getFindSales(searchRequest);
        return ResponseEntity.ok(ResponseHelper.success(result, "照会成功"));
    }
}
