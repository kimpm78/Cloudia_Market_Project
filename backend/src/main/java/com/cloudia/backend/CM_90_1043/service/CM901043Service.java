package com.cloudia.backend.CM_90_1043.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_90_1043.model.CategorySaveRequest;
import com.cloudia.backend.CM_90_1043.model.ResponseModel;

public interface CM901043Service {
    /**
     * 카테고리 전체 리스트 조회
     * 
     * @return 카테고리 전체 리스트
     */
    ResponseEntity<ResponseModel<Map<String, Object>>> findByAllCategory();

    /**
     * 카테고리 변경사항 저장 (추가/수정/삭제)
     */
    ResponseEntity<ResponseModel<String>> saveChanges(CategorySaveRequest request, String userId);
}
