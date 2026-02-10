package com.cloudia.backend.CM_01_1002.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;

import com.cloudia.backend.common.model.ResponseModel;

public interface CM011002Service {

    /**
     * メールアドレスに認証コードを送信
     * 
     * @param 登録有無を確認するユーザーのメールアドレス
     * @return 'message'が含まれた応答
     */
    ResponseEntity<ResponseModel<Map<String, String>>> sendVerificationCodeForFindId(String email);

    /**
     * メールアドレスと認証コードを検証し、ログインIDを返す
     * 
     * @param code ユーザーが入力した認証コード
     * @return 'loginId'が含まれた応答
     */
    ResponseEntity<ResponseModel<Map<String, Object>>> verifyAndFindId(String email, String code);
}