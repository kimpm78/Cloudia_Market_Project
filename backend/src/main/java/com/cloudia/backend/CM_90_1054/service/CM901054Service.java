package com.cloudia.backend.CM_90_1054.service;

import java.util.List;

import com.cloudia.backend.CM_90_1054.model.SalesDto;

public interface CM901054Service {
    /**
     * 期間別の月別売上取得
     * 
     * @param startMonth 開始月
     * @param endMonth   終了月
     * @return 期間別の月別売上リスト
     */
    List<SalesDto> getChart1(String startDate, String endDate);

    /**
     * 期間別の月別純利益取得
     * 
     * @param startMonth 開始月
     * @param endMonth   終了月
     * @return 期間別の月別純利益リスト
     */
    List<SalesDto> getChart2(String startDate, String endDate);
}