package com.cloudia.backend.CM_90_1031.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_90_1031.model.ageDto;
import com.cloudia.backend.CM_90_1031.model.genderDto;

public interface CM901031Service {
    /**
     * 年齢層一覧取得
     * 
     * @return 年齢層一覧
     */
    ResponseEntity<ResponseModel<List<ageDto>>> findByAllAges();

    /**
     * 性別一覧取得
     * 
     * @return 性別一覧
     */
    ResponseEntity<ResponseModel<List<genderDto>>> findByAllGenders();
}
