package com.cloudia.backend.CM_90_1010.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1010.model.PreviousInfoDto;
import com.cloudia.backend.CM_90_1010.model.StatusDto;
import com.cloudia.backend.CM_90_1010.model.WeeklySalesDto;

@Mapper
public interface CM901010Mapper {
    /**
     * 현재 상태(주문,취소등) 조회
     * 
     * @return 현재 상태 리스트
     */
    StatusDto getStatus(@Param("startDate") String startDate, @Param("EndDate") String EndDate);

    /**
     * 전날 정보 조회
     * 
     * @return 전날 정보
     */
    PreviousInfoDto getPreviousInfo(@Param("startDate") String startDate, @Param("EndDate") String EndDate);

    /**
     * 일주일 매출 조회
     * 
     * @return 일주일 매출 리스트
     */
    List<WeeklySalesDto> getWeeklySales(@Param("startDate") String startDate, @Param("EndDate") String EndDate);
}
