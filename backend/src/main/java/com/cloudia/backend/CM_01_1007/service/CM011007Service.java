package com.cloudia.backend.CM_01_1007.service;

import com.cloudia.backend.CM_01_1007.model.UserProfile;
import org.springframework.http.ResponseEntity;
import java.util.Map;

public interface CM011007Service {
    /**
     * 사용자의 프로필 정보를 조회
     * 
     * @param loginId 조회할 사용자의 로그인 ID
     * @return 프로필 정보를 담은 ResponseEntity 객체
     */
    ResponseEntity<UserProfile> getProfile(String loginId);

    /**
     * 사용자의 프로필 정보를 조회
     * 
     * @param loginId 조회할 사용자의 로그인 ID
     * @return 프로필 정보를 담은 ResponseEntity 객체
     */
    ResponseEntity<Map<String, Object>> updateProfile(String loginId, UserProfile profileDTO);
}