package com.cloudia.backend.CM_90_1010.service.impl;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_90_1010.mapper.CM901010Mapper;
import com.cloudia.backend.CM_90_1010.model.PreviousInfoDto;
import com.cloudia.backend.CM_90_1010.model.StatusDto;
import com.cloudia.backend.CM_90_1010.model.WeeklySalesDto;
import com.cloudia.backend.CM_90_1010.service.CM901010Service;
import com.cloudia.backend.common.log.LogHelper;
import com.cloudia.backend.common.log.LogMessage;
import com.cloudia.backend.common.util.DateCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM901010ServiceImpl implements CM901010Service {
    private final CM901010Mapper cm901010Mapper;
    private final DateCalculator dateCalculator;

    /**
     * 현재 상태(주문,취소등) 조회
     * 
     * @return 현재 상태 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public StatusDto getStatus() {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "관리자 메인화면" });

        StatusDto responseList = cm901010Mapper.getStatus(
                dateCalculator.convertToYYYYMMDD(dateCalculator.getCurrentWeekStartDate()),
                dateCalculator.convertToYYYYMMDD(dateCalculator.getCurrentWeekEndDate()));

        return responseList;
    }

    /**
     * 전날 정보 조회
     * 
     * @return 전날 정보
     */
    @Override
    @Transactional(readOnly = true)
    public PreviousInfoDto getPreviousInfo() {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "관리자 메인화면" });

        PreviousInfoDto responseList = cm901010Mapper.getPreviousInfo(
                dateCalculator.nextDay(dateCalculator.DateString(), -1),
                dateCalculator.DateString());

        return responseList;
    }

    /**
     * 일주일 매출 조회
     * 
     * @return 일주일 매출 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<WeeklySalesDto> getWeeklySales() {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "管理者 メイン画面" });
        List<WeeklySalesDto> responseList = cm901010Mapper.getWeeklySales(
                dateCalculator.convertToYYYYMMDD(dateCalculator.getCurrentWeekStartDate()),
                dateCalculator.convertToYYYYMMDD(dateCalculator.getCurrentWeekEndDate()));

        if (responseList == null) {
            responseList = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "管理者 メイン画面", String.valueOf(responseList.size()) });

        return responseList;
    }
}
