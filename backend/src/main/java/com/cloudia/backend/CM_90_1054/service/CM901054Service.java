package com.cloudia.backend.CM_90_1054.service;

import java.util.List;

import com.cloudia.backend.CM_90_1054.model.SalesDto;

public interface CM901054Service {
    /**
     * 기간별 월 매출 조회
     * 
     * @param startMonth 시작 월
     * @param endMonth   끝 월
     * @return 기간별 월 매출 리스트
     */
    List<SalesDto> getChart1(String startDate, String endDate);

    /**
     * 기간별 월 순수익 조회
     * 
     * @param startMonth 시작 월
     * @param endMonth   끝 월
     * @return 기간별 월 순수익 리스트
     */
    List<SalesDto> getChart2(String startDate, String endDate);
}