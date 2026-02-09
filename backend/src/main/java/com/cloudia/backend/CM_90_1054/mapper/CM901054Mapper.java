package com.cloudia.backend.CM_90_1054.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1054.model.SalesDto;

@Mapper
public interface CM901054Mapper {
    /**
     * 기간별 월 매출 조회
     * 
     * @param startMonth 시작 월
     * @param endMonth   끝 월
     * @return 기간별 월 매출 리스트
     */
    List<SalesDto> getChart1(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 기간별 월 순수익 조회
     * 
     * @param startMonth 시작 월
     * @param endMonth   끝 월
     * @return 기간별 월 순수익 리스트
     */
    List<SalesDto> getChart2(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
