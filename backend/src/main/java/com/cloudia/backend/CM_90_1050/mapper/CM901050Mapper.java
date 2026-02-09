package com.cloudia.backend.CM_90_1050.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.cloudia.backend.CM_90_1050.model.ResultDto;
import com.cloudia.backend.CM_90_1050.model.SearchRequestDto;

@Mapper
public interface CM901050Mapper {
    /**
     * 매출 정보 리스트 조회
     * 
     * @return 매출 정보 전체 리스트
     */
    List<ResultDto> findByAllSales();

    /**
     * 특정 매출 정보 리스트 조회
     * 
     * @param 검색 데이터
     * @return 특정 매출 정보 리스트
     */
    List<ResultDto> getFindSales(SearchRequestDto searchRequest);
}
