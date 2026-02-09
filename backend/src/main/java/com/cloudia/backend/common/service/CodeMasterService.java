package com.cloudia.backend.common.service;

import java.util.List;

import com.cloudia.backend.common.model.CodeMaster;

public interface CodeMasterService {

    /**
     * 특정 코드 값을 가진 단일 코드를 조회
     * 
     * @param codeType  코드 타입
     * @param codeValue 코드 값
     * @return CodeMaster 객체
     */
    CodeMaster getCodeByValue(String codeType, int codeValue);

    /**
     * 특정 코드 타입을 가진 코드 목록 전체를 조회
     *
     * @param codeType 코드 타입
     * @return CodeMaster 객체 리스트
     */
    List<CodeMaster> getCodesByType(String codeType);

}