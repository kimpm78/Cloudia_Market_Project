package com.cloudia.backend.CM_01_1003.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.cloudia.backend.CM_01_1003.model.ResetPasswordRequest;
import com.cloudia.backend.common.model.ResponseModel;

public interface CM011003Service {
    /**
     * パスワード再設定のため、登録済みメールアドレス宛に認証コードを送信
     */
    ResponseEntity<ResponseModel<Map<String, Object>>> sendVerificationCodeForFindPw(ResetPasswordRequest request);

    /**
     * ユーザーが入力した認証コードを検証
     */
    ResponseEntity<ResponseModel<Map<String, Object>>> verifyCodeForFindPw(String email, String code);

    /**
     * 認証済みユーザーのパスワードを再設定
     */
    ResponseEntity<ResponseModel<Map<String, String>>> resetPassword(ResetPasswordRequest request);
}