package com.cloudia.backend.CM_01_1007.service;

import com.cloudia.backend.CM_01_1007.model.UserProfile;
import org.springframework.http.ResponseEntity;
import java.util.Map;

public interface CM011007Service {
    /**
     * ユーザーのプロフィール情報を取得
     * 
     * @param loginId 取得対象ユーザーのログインID
     * @return プロフィール情報を含む ResponseEntity オブジェクト
     */
    ResponseEntity<UserProfile> getProfile(String loginId);

    /**
     * ユーザーのプロフィール情報を更新
     * 
     * @param loginId 更新対象ユーザーのログインID
     * @param profileDTO 更新するプロフィール情報
     * @return 更新結果を含む ResponseEntity オブジェクト
     */
    ResponseEntity<Map<String, Object>> updateProfile(String loginId, UserProfile profileDTO);
}