package com.cloudia.backend.CM_90_1031.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_90_1031.model.ResponseModel;
import com.cloudia.backend.CM_90_1031.model.ageDto;
import com.cloudia.backend.CM_90_1031.model.genderDto;

public interface CM901031Service {
    /**
     * 연령대 리스트 조회
     * 
     * @return 연령대 리스트
     */
    ResponseEntity<ResponseModel<List<ageDto>>> findByAllAges();

    /**
     * 성별 리스트 조회
     * 
     * @return 성별 리스트
     */
    ResponseEntity<ResponseModel<List<genderDto>>> findByAllGenders();
}
