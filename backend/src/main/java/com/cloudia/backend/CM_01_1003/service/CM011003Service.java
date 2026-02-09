package com.cloudia.backend.CM_01_1003.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.cloudia.backend.CM_01_1003.model.ResetPasswordRequest;
import com.cloudia.backend.common.model.ResponseModel;

public interface CM011003Service {
    /**
     * 비밀번호 찾기를 위해 가입된 이메일로 인증 코드를 발송
     */
    ResponseEntity<ResponseModel<Map<String, Object>>> sendVerificationCodeForFindPw(ResetPasswordRequest request);

    /**
     * 사용자가 입력한 인증 코드를 검증
     */
    ResponseEntity<ResponseModel<Map<String, Object>>> verifyCodeForFindPw(String email, String code);

    /**
     * 인증된 사용자의 비밀번호를 재설정
     */
    ResponseEntity<ResponseModel<Map<String, String>>> resetPassword(ResetPasswordRequest request);
}