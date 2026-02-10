package com.cloudia.backend.CM_90_1054.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1054.model.SalesDto;

@Mapper
public interface CM901054Mapper {
    /**
     * 期間別の月別売上取得
     * 
     * @param startMonth 開始月
     * @param endMonth   終了月
     * @return 期間別の月別売上リスト
     */
    List<SalesDto> getChart1(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 期間別の月別純利益取得
     * 
     * @param startMonth 開始月
     * @param endMonth   終了月
     * @return 期間別の月別純利益リスト
     */
    List<SalesDto> getChart2(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
