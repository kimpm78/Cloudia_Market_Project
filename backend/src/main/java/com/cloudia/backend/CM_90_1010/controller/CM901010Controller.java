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

import com.cloudia.backend.CM_90_1010.model.PreviousInfo;
import com.cloudia.backend.CM_90_1010.model.Status;
import com.cloudia.backend.CM_90_1010.model.WeeklySales;
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
     * 全体ステータス取得
     *
     * @return 全体ステータス
     */
    @GetMapping("/status")
    public ResponseEntity<ResponseModel<Status>> getMethodName() {
        Status result = cm901010Service.getStatus();
        return ResponseEntity.ok(ResponseHelper.success(result, "取得に成功しました"));
    }

    /**
     * 週間売上取得
     *
     * @return 週間売上リスト
     */
    @GetMapping("/weekly")
    public ResponseEntity<ResponseModel<List<WeeklySales>>> getWeeklySales() {
        List<WeeklySales> result = cm901010Service.getWeeklySales();
        return ResponseEntity.ok(ResponseHelper.success(result, "取得に成功しました"));
    }

    /**
     * 前日情報取得
     *
     * @return 前日情報
     */
    @GetMapping("/previous")
    public ResponseEntity<ResponseModel<PreviousInfo>> getPreviousInfo() {
        PreviousInfo result = cm901010Service.getPreviousInfo();
        return ResponseEntity.ok(ResponseHelper.success(result, "取得に成功しました"));
    }

    /**
     * 訪問者（日別）取得
     *
     * @return 訪問者（日別）リスト
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
        return ResponseEntity.ok(ResponseHelper.success(data, "取得に成功しました"));
    }

    /**
     * 訪問者（月別）取得
     *
     * @return 訪問者（月別）リスト
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
        return ResponseEntity.ok(ResponseHelper.success(data, "取得に成功しました"));
    }
}
