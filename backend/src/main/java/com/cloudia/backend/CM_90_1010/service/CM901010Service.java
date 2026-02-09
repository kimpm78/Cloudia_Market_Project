package com.cloudia.backend.CM_90_1010.service;

import java.util.List;

import com.cloudia.backend.CM_90_1010.model.PreviousInfoDto;
import com.cloudia.backend.CM_90_1010.model.StatusDto;
import com.cloudia.backend.CM_90_1010.model.WeeklySalesDto;

public interface CM901010Service {
    /**
     * 현재 상태(주문,취소등) 조회
     * 
     * @return 현재 상태 리스트
     */
    StatusDto getStatus();

    /**
     * 전날 정보 조회
     * 
     * @return 전날 정보
     */
    PreviousInfoDto getPreviousInfo();

    /**
     * 일주일 매출 조회
     * 
     * @return 일주일 매출 리스트
     */
    List<WeeklySalesDto> getWeeklySales();
}