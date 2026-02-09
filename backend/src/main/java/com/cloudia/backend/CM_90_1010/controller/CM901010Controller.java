package com.cloudia.backend.CM_90_1010.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.CM_90_1010.model.PreviousInfoDto;
import com.cloudia.backend.CM_90_1010.model.StatusDto;
import com.cloudia.backend.CM_90_1010.model.WeeklySalesDto;
import com.cloudia.backend.CM_90_1010.service.CM901010Service;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.service.GoogleAnalyticsService;
import com.cloudia.backend.common.util.DateCalculator;
import com.cloudia.backend.common.util.ResponseHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/main")
public class CM901010Controller {
    private final GoogleAnalyticsService analyticsService;
    private final CM901010Service cm901010Service;
    private final DateCalculator dateCalculator;

    /**
     * 전체 상태 조회
     * 
     * @return 전체 상태
     */
    @GetMapping("/status")
    public ResponseEntity<ResponseModel<StatusDto>> getMethodName() {
        StatusDto result = cm901010Service.getStatus();
        return ResponseEntity.ok(ResponseHelper.success(result, "조회 성공"));
    }

    /**
     * 일주일 매출 조회
     * 
     * @return 일주일 매출 리스트
     */
    @GetMapping("/weekly")
    public ResponseEntity<ResponseModel<List<WeeklySalesDto>>> getWeeklySales() {
        List<WeeklySalesDto> result = cm901010Service.getWeeklySales();
        return ResponseEntity.ok(ResponseHelper.success(result, "조회 성공"));
    }

    /**
     * 전날 정보 조회
     * 
     * @return 전날 정보
     */
    @GetMapping("/previous")
    public ResponseEntity<ResponseModel<PreviousInfoDto>> getPreviousInfo() {
        PreviousInfoDto result = cm901010Service.getPreviousInfo();
        return ResponseEntity.ok(ResponseHelper.success(result, "조회 성공"));
    }

    /**
     * 방문자(일별) 조회
     * 
     * @return 방문자(일별) 리스트
     */
    @GetMapping("/weekly-visitors")
    public ResponseEntity<ResponseModel<Map<String, Object>>> getWeeklyVisitors(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = dateCalculator.getCurrentWeekStartDate();
        }
        if (endDate == null) {
            endDate = dateCalculator.getCurrentWeekEndDate();
        }

        Map<String, Object> data = analyticsService.getVisitorsByDayOfWeek(startDate, endDate);
        return ResponseEntity.ok(ResponseHelper.success(data, "조회 성공"));
    }

    /**
     * 방문자(월별) 조회
     * 
     * @return 방문자(월별) 리스트
     */
    @GetMapping("/monthly-visitors")
    public ResponseEntity<ResponseModel<Map<String, Object>>> getMonthlyVisitors(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = dateCalculator.getStartOfYear();
        }
        if (endDate == null) {
            endDate = dateCalculator.getEndOfYear();
        }

        Map<String, Object> data = analyticsService.getVisitorsByMonth(startDate, endDate);
        return ResponseEntity.ok(ResponseHelper.success(data, "조회 성공"));
    }
}
