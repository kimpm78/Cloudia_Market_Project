package com.cloudia.backend.CM_01_1003.controller;

import com.cloudia.backend.CM_01_1003.constants.CM011003MessageConstant;
import com.cloudia.backend.CM_01_1003.model.ResetPasswordRequest;
import com.cloudia.backend.CM_01_1003.service.CM011003Service;
import com.cloudia.backend.common.model.ResponseModel;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("CM_01_1003")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/guest")
public class CM011003Controller {

    private final CM011003Service CM011003Service;

    /**
     * 비밀번호 재설정 인증 코드 발송
     */
    @PostMapping("/reset-password/send-code")
    public ResponseEntity<ResponseModel<Map<String, Object>>> sendCode(@RequestBody ResetPasswordRequest request) {
        log.info(CM011003MessageConstant.SEND_CODE_REQUEST_START);

        ResponseEntity<ResponseModel<Map<String, Object>>> response = CM011003Service
                .sendVerificationCodeForFindPw(request);

        log.info(CM011003MessageConstant.SEND_CODE_REQUEST_END);
        return response;
    }

    /**
     * 비밀번호 재설정 인증 코드 검증
     */
    @PostMapping("/reset-password/verify")
    public ResponseEntity<ResponseModel<Map<String, Object>>> verifyCode(@RequestBody Map<String, String> request) {
        log.info(CM011003MessageConstant.VERIFY_CODE_REQUEST_START);

        ResponseEntity<ResponseModel<Map<String, Object>>> response = CM011003Service.verifyCodeForFindPw(
                request.get("email"),
                request.get("code"));

        log.info(CM011003MessageConstant.VERIFY_CODE_REQUEST_END);
        return response;
    }

    /**
     * 비밀번호 변경 실행
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ResponseModel<Map<String, String>>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info(CM011003MessageConstant.RESET_PASSWORD_REQUEST_START);

        ResponseEntity<ResponseModel<Map<String, String>>> response = CM011003Service.resetPassword(request);

        log.info(CM011003MessageConstant.RESET_PASSWORD_REQUEST_END);
        return response;
    }
}