package com.cloudia.backend.CM_90_1030.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.util.ResponseHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.common.service.GoogleAnalyticsService;
import com.cloudia.backend.common.util.DateCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/visits")
public class CM901030Controller {
    private final GoogleAnalyticsService analyticsService;
    private final DateCalculator dateCalculator;

    @GetMapping("/newReturning")
    public ResponseEntity<ResponseModel<Map<String, Long>>> getNewVsReturning(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = dateCalculator.convertToLocalDate(dateCalculator.tokyoTime());
        }
        if (endDate == null) {
            endDate = dateCalculator.convertToLocalDate(dateCalculator.tokyoTime());
        }

        Map<String, Long> data = analyticsService.getNewVsReturningUsers(startDate, endDate);

        return ResponseEntity.ok(ResponseHelper.success(data, "取得に成功しました"));
    }

    @GetMapping("/sessionsDevice")
    public ResponseEntity<ResponseModel<Map<String, Long>>> getSessionsByDevice(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = dateCalculator.convertToLocalDate(dateCalculator.tokyoTime());
        }
        if (endDate == null) {
            endDate = dateCalculator.convertToLocalDate(dateCalculator.tokyoTime());
        }

        Map<String, Long> data = analyticsService.getSessionsByDevice(startDate, endDate);
        return ResponseEntity.ok(ResponseHelper.success(data, "取得に成功しました"));
    }

    @GetMapping("/sessionsChannel")
    public ResponseEntity<ResponseModel<List<Map<String, Object>>>> getSessionsByChannel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = dateCalculator.convertToLocalDate(dateCalculator.tokyoTime());
        }
        if (endDate == null) {
            endDate = dateCalculator.convertToLocalDate(dateCalculator.tokyoTime());
        }

        List<Map<String, Object>> data = analyticsService.getSessionsByChannel(startDate, endDate);
        return ResponseEntity.ok(ResponseHelper.success(data, "取得に成功しました"));
    }

    @GetMapping("/pageEngagement")
    public ResponseEntity<ResponseModel<List<Map<String, Object>>>> getPageEngagementTime(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = dateCalculator.convertToLocalDate(dateCalculator.tokyoTime());
        }
        if (endDate == null) {
            endDate = dateCalculator.convertToLocalDate(dateCalculator.tokyoTime());
        }

        List<Map<String, Object>> data = analyticsService.getAverageEngagementTimeByPage(startDate, endDate);
        return ResponseEntity.ok(ResponseHelper.success(data, "取得に成功しました"));
    }
}
