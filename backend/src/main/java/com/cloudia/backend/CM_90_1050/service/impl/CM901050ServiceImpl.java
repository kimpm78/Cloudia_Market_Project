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
     * 売上情報全件一覧取得
     * 
     * @return 売上情報全件一覧
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResultDto> findByAllSales() {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "売上情報（表）" });

        List<ResultDto> responseList = cm901050Mapper.findByAllSales();

        if (responseList.size() == 0) {
            responseList = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "売上情報（表）", String.valueOf(responseList.size()) });

        return responseList;
    }

    /**
     * 条件指定売上情報一覧取得
     * 
     * @param searchRequest 検索条件
     * @return 売上情報一覧
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResultDto> getFindSales(SearchRequestDto searchRequest) {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "売上情報（表）" });

        List<ResultDto> responseList = cm901050Mapper.getFindSales(searchRequest);

        if (responseList.size() == 0) {
            responseList = Collections.emptyList();
        }
        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "売上情報（表）", String.valueOf(responseList.size()) });

        return responseList;
    }
}