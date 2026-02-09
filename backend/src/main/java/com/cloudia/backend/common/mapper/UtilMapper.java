package com.cloudia.backend.common.mapper;

import java.time.LocalDate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UtilMapper {
    // 공휴일 체크
    boolean isHoliday(@Param("holidayDate") LocalDate holidayDate,
            @Param("countryCode") String countryCode);
}
