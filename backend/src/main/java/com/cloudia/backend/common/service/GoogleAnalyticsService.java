package com.cloudia.backend.common.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface GoogleAnalyticsService {
    void init() throws Exception;

    Long getTotalSessions(LocalDate startDate, LocalDate endDate);

    // 曜日別の訪問者数（PV, UV）
    Map<String, Object> getVisitorsByDayOfWeek(LocalDate startDate, LocalDate endDate);

    // 月別の訪問者数（PV, UV）
    Map<String, Object> getVisitorsByMonth(LocalDate startDate, LocalDate endDate);

    // 新規／リピーター数
    Map<String, Long> getNewVsReturningUsers(LocalDate startDate, LocalDate endDate);

    // デバイス別セッション数
    Map<String, Long> getSessionsByDevice(LocalDate startDate, LocalDate endDate);

    // チャネル別セッション数（流入経路）
    List<Map<String, Object>> getSessionsByChannel(LocalDate startDate, LocalDate endDate);

    // ページ別の平均滞在時間（分単位）
    List<Map<String, Object>> getAverageEngagementTimeByPage(LocalDate startDate, LocalDate endDate);

}
