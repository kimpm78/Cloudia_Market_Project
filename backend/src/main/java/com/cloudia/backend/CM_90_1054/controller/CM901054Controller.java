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
    // Service 정의
    private final CM901054Service cm901054Service;

    /**
     * 기간별 월 매출 조회
     * 
     * @param startMonth 시작 월
     * @param endMonth   끝 월
     * @return 기간별 월 매출 리스트
     */
    @GetMapping("/chart1")
    public ResponseEntity<ResponseModel<List<SalesDto>>> getChart1(
            @RequestParam String startMonth,
            @RequestParam String endMonth) {
        List<SalesDto> chart = cm901054Service.getChart1(startMonth, endMonth);
        return ResponseEntity.ok(ResponseHelper.success(chart, "조회 성공"));
    }

    /**
     * 기간별 월 순수익 조회
     * 
     * @param startMonth 시작 월
     * @param endMonth   끝 월
     * @return 기간별 월 순수익 리스트
     */
    @GetMapping("/chart2")
    public ResponseEntity<ResponseModel<List<SalesDto>>> getChart2(
            @RequestParam String startMonth,
            @RequestParam String endMonth) {
        List<SalesDto> chart = cm901054Service.getChart2(startMonth, endMonth);
        return ResponseEntity.ok(ResponseHelper.success(chart, "조회 성공"));
    }
}
