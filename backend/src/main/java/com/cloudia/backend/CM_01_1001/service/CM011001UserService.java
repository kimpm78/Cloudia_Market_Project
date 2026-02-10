package com.cloudia.backend.CM_01_1001.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_01_1001.model.SignUpRequestModel;

public interface CM011001UserService {

    /**
     * 新しいユーザーを登録
     * 
     * @param request ユーザー登録データ
     * @return 登録された SignUpRequestModel オブジェクト
     */
    ResponseEntity<Map<String, Object>> signUp(SignUpRequestModel request);

    /**
     * ログイン IDの使用可能 여부を確認
     *
     * @param loginId 重複有無を確認するログインID
     * @return {@code ResponseEntity<Integer>}
     */

    ResponseEntity<Integer> checkLoginId(String loginId);

    /**
     * メールアドレスの確認
     * @param email 確認するメールアドレス
     * 
     * @return 使用可能であれば true、既に存在すれば false
     */
    boolean isEmailAvailable(String email);

    /**
     * 個人通関固有符号（PCCC）の利用可否を確認
     *
     * @param pccc 重複有無を確認するPCCC番号
     * @return {@code ResponseEntity<Integer>}
     */
    ResponseEntity<Integer> checkPccc(String pccc);

}