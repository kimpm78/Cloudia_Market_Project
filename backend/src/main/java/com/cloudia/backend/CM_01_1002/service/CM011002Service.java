package com.cloudia.backend.CM_01_1002.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;

import com.cloudia.backend.common.model.ResponseModel;

public interface CM011002Service {

    /**
     * * @param email 가입 여부를 확인할 사용자의 이메일
     * 
     * @return 발송 성공 또는 실패에 대한 응답
     */
    ResponseEntity<ResponseModel<Map<String, String>>> sendVerificationCodeForFindId(String email);

    /**
     * * @param email 사용자의 이메일
     * 
     * @param code 사용자가 입력한 인증 코드
     * @return 'loginId'가 포함된 응답
     */
    ResponseEntity<ResponseModel<Map<String, Object>>> verifyAndFindId(String email, String code);
}