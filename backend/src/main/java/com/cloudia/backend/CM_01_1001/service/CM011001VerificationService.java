package com.cloudia.backend.CM_01_1001.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.cloudia.backend.common.model.ResponseModel;

public interface CM011001VerificationService {

    /**
     * 認証コードを生成し、メールで送信
     */
    ResponseEntity<ResponseModel<Map<String, String>>> sendVerificationEmail(String email);

    /**
     * ユーザーが入力した認証コードを検証
     */
    ResponseEntity<ResponseModel<Map<String, Object>>> verifyEmail(String email, String code);

    /**
     * メールアドレスの認証状態を確認
     */
    boolean isEmailVerified(String email);

    /**
     * 認証状態を初期化
     */
    void clearVerificationStatus(String email);
}