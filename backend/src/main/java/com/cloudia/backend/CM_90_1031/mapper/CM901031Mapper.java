package com.cloudia.backend.CM_90_1031.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.cloudia.backend.CM_90_1031.model.ageDto;
import com.cloudia.backend.CM_90_1031.model.genderDto;

@Mapper
public interface CM901031Mapper {
    /**
     * 年齢層一覧取得
     * 
     * @return 年齢層一覧
     */
    List<ageDto> findByAllAges();

    /**
     * 性別一覧取得
     * 
     * @return 性別一覧
     */
    List<genderDto> findByAllGenders();
}
