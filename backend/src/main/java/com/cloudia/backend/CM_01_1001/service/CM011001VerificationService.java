package com.cloudia.backend.CM_01_1001.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.cloudia.backend.common.model.ResponseModel;

public interface CM011001VerificationService {

    /**
     * 인증 코드를 생성하고 이메일로 발송
     */
    ResponseEntity<ResponseModel<Map<String, String>>> sendVerificationEmail(String email);

    /**
     * 사용자가 입력한 인증 코드 검증
     */
    ResponseEntity<ResponseModel<Map<String, Object>>> verifyEmail(String email, String code);

    /**
     * 이메일 인증 여부 확인
     */
    boolean isEmailVerified(String email);

    /**
     * 인증 상태 초기화
     */
    void clearVerificationStatus(String email);
}