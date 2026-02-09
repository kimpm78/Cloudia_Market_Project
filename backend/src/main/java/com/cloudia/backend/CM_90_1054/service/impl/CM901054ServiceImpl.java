package com.cloudia.backend.CM_90_1054.service.impl;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_90_1054.mapper.CM901054Mapper;
import com.cloudia.backend.CM_90_1054.model.SalesDto;
import com.cloudia.backend.CM_90_1054.service.CM901054Service;
import com.cloudia.backend.common.log.LogHelper;
import com.cloudia.backend.common.log.LogMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM901054ServiceImpl implements CM901054Service {

    private final CM901054Mapper cm901054Mapper;
    /**
     * 기간별 월 매출 조회
     * 
     * @param startMonth 시작 월
     * @param endMonth   끝 월
     * @return 기간별 월 매출 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<SalesDto> getChart1(String startDate, String endDate) {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "매출 정보(차트)" });

        List<SalesDto> responseList = cm901054Mapper.getChart1(startDate, endDate);

        if (responseList.size() == 0) {
            responseList = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "매출 정보(차트)", String.valueOf(responseList.size()) });

        return responseList;
    }

    /**
     * 기간별 월 순수익 조회
     * 
     * @param startMonth 시작 월
     * @param endMonth   끝 월
     * @return 기간별 월 순수익 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<SalesDto> getChart2(String startDate, String endDate) {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "매출 정보(기간별)" });

        List<SalesDto> responseList = cm901054Mapper.getChart2(startDate, endDate);

        if (responseList.size() == 0) {
            responseList = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "매출 정보(기간별)", String.valueOf(responseList.size()) });

        return responseList;
    }
}
