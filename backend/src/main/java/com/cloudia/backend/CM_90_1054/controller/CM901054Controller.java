package com.cloudia.backend.CM_90_1054.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.CM_90_1054.model.SalesDto;
import com.cloudia.backend.CM_90_1054.service.CM901054Service;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.util.ResponseHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/settlement/month-sales")
public class CM901054Controller {
    private final CM901054Service cm901054Service;

    /**
     * 期間別の月別売上取得
     * 
     * @param startMonth 開始月
     * @param endMonth   終了月
     * @return 期間別の月別売上リスト
     */
    @GetMapping("/chart1")
    public ResponseEntity<ResponseModel<List<SalesDto>>> getChart1(
            @RequestParam String startMonth,
            @RequestParam String endMonth) {
        List<SalesDto> chart = cm901054Service.getChart1(startMonth, endMonth);
        return ResponseEntity.ok(ResponseHelper.success(chart, "取得成功"));
    }

    /**
     * 期間別の月別純利益取得
     * 
     * @param startMonth 開始月
     * @param endMonth   終了月
     * @return 期間別の月別純利益リスト
     */
    @GetMapping("/chart2")
    public ResponseEntity<ResponseModel<List<SalesDto>>> getChart2(
            @RequestParam String startMonth,
            @RequestParam String endMonth) {
        List<SalesDto> chart = cm901054Service.getChart2(startMonth, endMonth);
        return ResponseEntity.ok(ResponseHelper.success(chart, "取得成功"));
    }
}
