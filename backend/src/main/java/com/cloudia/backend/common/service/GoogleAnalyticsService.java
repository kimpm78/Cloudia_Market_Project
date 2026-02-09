package com.cloudia.backend.common.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface GoogleAnalyticsService {
    void init() throws Exception;

    Long getTotalSessions(LocalDate startDate, LocalDate endDate);

    // 요일별 방문자 수 (PV, UV)
    Map<String, Object> getVisitorsByDayOfWeek(LocalDate startDate, LocalDate endDate);

    // 월별 방문자 수 (PV, UV)
    Map<String, Object> getVisitorsByMonth(LocalDate startDate, LocalDate endDate);

    // 신규 재방문자 수
    Map<String, Long> getNewVsReturningUsers(LocalDate startDate, LocalDate endDate);

    // 기기별 세션
    Map<String, Long> getSessionsByDevice(LocalDate startDate, LocalDate endDate);

    // 채널별 세션 (유입 경로)
    List<Map<String, Object>> getSessionsByChannel(LocalDate startDate, LocalDate endDate);

    // 페이지별 평균 머문 시간 (분 단위)
    List<Map<String, Object>> getAverageEngagementTimeByPage(LocalDate startDate, LocalDate endDate);

}
