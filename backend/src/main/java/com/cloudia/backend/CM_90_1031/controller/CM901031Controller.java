package com.cloudia.backend.CM_90_1031.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_90_1031.model.ageDto;
import com.cloudia.backend.CM_90_1031.model.genderDto;
import com.cloudia.backend.CM_90_1031.service.CM901031Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/visits")
public class CM901031Controller {
    @Autowired
    CM901031Service cm901031Service;

    /**
     * 年齢層一覧取得
     * 
     * @return 年齢層一覧
     */
    @GetMapping("/findAllAges")
    public ResponseEntity<ResponseModel<List<ageDto>>> getFindAllAges() {
        return cm901031Service.findByAllAges();
    }

    /**
     * 性別一覧取得
     * 
     * @return 性別一覧
     */
    @GetMapping("/findAllGenders")
    public ResponseEntity<ResponseModel<List<genderDto>>> getFindAllGenders() {
        return cm901031Service.findByAllGenders();
    }
}
