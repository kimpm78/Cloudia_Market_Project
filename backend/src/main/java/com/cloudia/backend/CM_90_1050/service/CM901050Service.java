package com.cloudia.backend.CM_90_1050.service;

import java.util.List;

import com.cloudia.backend.CM_90_1050.model.ResultDto;
import com.cloudia.backend.CM_90_1050.model.SearchRequestDto;

public interface CM901050Service {
    /**
     * 매출 정보 리스트 조회
     * 
     * @return 매출 정보 전체 리스트
     */
    List<ResultDto> findByAllSales();

    /**
     * 특정 매출 정보 리스트 조회
     * 
     * @return 특정 매출 정보 리스트
     */
    List<ResultDto> getFindSales(SearchRequestDto searchRequest);
}
