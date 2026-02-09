package com.cloudia.backend.CM_90_1031.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.cloudia.backend.CM_90_1031.model.ageDto;
import com.cloudia.backend.CM_90_1031.model.genderDto;

@Mapper
public interface CM901031Mapper {
    /**
     * 연령대 리스트 조회
     * 
     * @return 연령대 리스트
     */
    List<ageDto> findByAllAges();

    /**
     * 성별 리스트 조회
     * 
     * @return 성별 리스트
     */
    List<genderDto> findByAllGenders();
}
