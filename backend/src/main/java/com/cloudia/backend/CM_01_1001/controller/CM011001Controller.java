package com.cloudia.backend.CM_01_1001.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cloudia.backend.CM_01_1001.model.SignUpRequestModel;
import com.cloudia.backend.CM_01_1001.service.CM011001UserService;
import com.cloudia.backend.CM_01_1001.service.impl.CM011001VerificationServiceImpl;
import com.cloudia.backend.common.model.ResponseModel;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import com.cloudia.backend.CM_01_1001.constants.CM011001MessageConstant;

@RestController("CM_01_1001")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/guest")
public class CM011001Controller {

    private final CM011001UserService userService;
    private final CM011001VerificationServiceImpl verificationService;

    /**
     * 会員登録処理
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signUp(@Valid @RequestBody SignUpRequestModel request) {
        log.info(CM011001MessageConstant.SIGNUP_START);
        ResponseEntity<Map<String, Object>> response = userService.signUp(request);
        log.info(CM011001MessageConstant.SIGNUP_END);
        return response;
    }

    /**
     * ID重複チェック
     */
    @GetMapping("/check-id")
    public ResponseEntity<Integer> checkLoginId(@RequestParam String loginId) {
        log.info(CM011001MessageConstant.ID_CHECK_START);
        ResponseEntity<Integer> response = userService.checkLoginId(loginId);
        log.info(CM011001MessageConstant.ID_CHECK_END);
        return response;
    }

    /**
     * メール認証コード送信
     */
    @PostMapping("/send-verification-email")
    public ResponseEntity<ResponseModel<Map<String, String>>> sendVerificationEmail(
            @RequestBody Map<String, String> request) {
        String email = request.get("email");
        log.info(CM011001MessageConstant.EMAIL_SEND_START, email);
        ResponseEntity<ResponseModel<Map<String, String>>> response = verificationService.sendVerificationEmail(email);
        log.info(CM011001MessageConstant.EMAIL_SEND_END);
        return response;
    }

    /**
     * メール認証コード検証
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ResponseModel<Map<String, Object>>> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        log.info(CM011001MessageConstant.EMAIL_VERIFY_START, email);

        ResponseEntity<ResponseModel<Map<String, Object>>> response = verificationService.verifyEmail(
                email,
                request.get("code"));
        log.info(CM011001MessageConstant.EMAIL_VERIFY_END);
        return response;
    }
}