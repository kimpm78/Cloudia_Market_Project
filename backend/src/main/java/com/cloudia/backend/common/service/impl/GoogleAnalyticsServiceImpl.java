package com.cloudia.backend.common.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.cloudia.backend.common.service.GoogleAnalyticsService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GoogleAnalyticsServiceImpl implements GoogleAnalyticsService {

    @Override
    public void init() {
        log.info("Google Analytics real integration is not configured. Using fallback service.");
    }

    @Override
    public Long getTotalSessions(LocalDate startDate, LocalDate endDate) {
        return 0L;
    }

    @Override
    public Map<String, Object> getVisitorsByDayOfWeek(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pv", fixedZeroList(7));
        result.put("uv", fixedZeroList(7));
        return result;
    }

    @Override
    public Map<String, Object> getVisitorsByMonth(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pv", fixedZeroList(12));
        result.put("uv", fixedZeroList(12));
        return result;
    }

    @Override
    public Map<String, Long> getNewVsReturningUsers(LocalDate startDate, LocalDate endDate) {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("newUsers", 0L);
        result.put("returningUsers", 0L);
        return result;
    }

    @Override
    public Map<String, Long> getSessionsByDevice(LocalDate startDate, LocalDate endDate) {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("desktop", 0L);
        result.put("mobile", 0L);
        result.put("tablet", 0L);
        return result;
    }

    @Override
    public List<Map<String, Object>> getSessionsByChannel(LocalDate startDate, LocalDate endDate) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getAverageEngagementTimeByPage(LocalDate startDate, LocalDate endDate) {
        return new ArrayList<>();
    }

    private List<Long> fixedZeroList(int length) {
        List<Long> values = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            values.add(0L);
        }
        return values;
    }
}
