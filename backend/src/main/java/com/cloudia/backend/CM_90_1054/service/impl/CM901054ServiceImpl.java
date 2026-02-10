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
     * 期間別の月別売上取得
     * 
     * @param startMonth 開始月
     * @param endMonth   終了月
     * @return 期間別の月別売上リスト
     */
    @Override
    @Transactional(readOnly = true)
    public List<SalesDto> getChart1(String startDate, String endDate) {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "売上情報（チャート）" });

        List<SalesDto> responseList = cm901054Mapper.getChart1(startDate, endDate);

        if (responseList.size() == 0) {
            responseList = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "売上情報（チャート）", String.valueOf(responseList.size()) });

        return responseList;
    }

    /**
     * 期間別の月別純利益取得
     * 
     * @param startMonth 開始月
     * @param endMonth   終了月
     * @return 期間別の月別純利益リスト
     */
    @Override
    @Transactional(readOnly = true)
    public List<SalesDto> getChart2(String startDate, String endDate) {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "売上情報（期間別）" });

        List<SalesDto> responseList = cm901054Mapper.getChart2(startDate, endDate);

        if (responseList.size() == 0) {
            responseList = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "売上情報（期間別）", String.valueOf(responseList.size()) });

        return responseList;
    }
}
