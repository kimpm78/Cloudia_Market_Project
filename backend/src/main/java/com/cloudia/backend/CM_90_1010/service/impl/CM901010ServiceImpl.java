package com.cloudia.backend.CM_90_1010.service.impl;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_90_1010.mapper.CM901010Mapper;
import com.cloudia.backend.CM_90_1010.model.PreviousInfo;
import com.cloudia.backend.CM_90_1010.model.Status;
import com.cloudia.backend.CM_90_1010.model.WeeklySales;
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
     * 現在のステータス（注文・キャンセル等）を取得
     * 
     * @return 現在のステータス
     */
    @Override
    @Transactional(readOnly = true)
    public Status getStatus() {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "管理者 メイン画面" });

        Status responseList = cm901010Mapper.getStatus(
                dateCalculator.convertToYYYYMMDD(dateCalculator.getCurrentWeekStartDate()),
                dateCalculator.convertToYYYYMMDD(dateCalculator.getCurrentWeekEndDate()));

        return responseList;
    }

    /**
     * 前日情報を取得
     * 
     * @return 前日情報
     */
    @Override
    @Transactional(readOnly = true)
    public PreviousInfo getPreviousInfo() {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "管理者 メイン画面" });

        PreviousInfo responseList = cm901010Mapper.getPreviousInfo(
                dateCalculator.nextDay(dateCalculator.DateString(), -1),
                dateCalculator.DateString());

        return responseList;
    }

    /**
     * 週間売上を取得
     * 
     * @return 週間売上一覧
     */
    @Override
    @Transactional(readOnly = true)
    public List<WeeklySales> getWeeklySales() {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "管理者 メイン画面" });
        List<WeeklySales> responseList = cm901010Mapper.getWeeklySales(
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
