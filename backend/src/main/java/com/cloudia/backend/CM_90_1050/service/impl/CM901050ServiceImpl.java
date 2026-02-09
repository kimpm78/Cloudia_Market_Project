package com.cloudia.backend.CM_90_1050.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_90_1050.mapper.CM901050Mapper;
import com.cloudia.backend.CM_90_1050.model.ResultDto;
import com.cloudia.backend.CM_90_1050.service.CM901050Service;
import com.cloudia.backend.common.log.LogHelper;
import com.cloudia.backend.common.log.LogMessage;
import com.cloudia.backend.CM_90_1050.model.SearchRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM901050ServiceImpl implements CM901050Service {
    private final CM901050Mapper cm901050Mapper;

    /**
     * 매출 정보 전체 리스트 조회
     * 
     * @return 매출 정보 전체 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResultDto> findByAllSales() {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "매출 정보(표)" });

        List<ResultDto> responseList = cm901050Mapper.findByAllSales();

        if (responseList.size() == 0) {
            responseList = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "매출 정보(표)", String.valueOf(responseList.size()) });

        return responseList;
    }

    /**
     * 특정 매출 정보 리스트 조회
     * 
     * @param searchRequest 검색 조건
     * @return 특정 매출 정보 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResultDto> getFindSales(SearchRequestDto searchRequest) {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "매출 정보(표)" });

        List<ResultDto> responseList = cm901050Mapper.getFindSales(searchRequest);

        if (responseList.size() == 0) {
            responseList = Collections.emptyList();
        }
        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "매출 정보(표)", String.valueOf(responseList.size()) });

        return responseList;
    }
}