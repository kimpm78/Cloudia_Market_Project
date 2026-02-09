package com.cloudia.backend.CM_01_1009.service;

import com.cloudia.backend.CM_01_1009.model.ChangePassword;
import org.springframework.http.ResponseEntity;
import java.util.Map;

public interface CM011009Service {
    /**
     * @param loginId 변경을 요청한 사용자의 로그인 ID
     * @param request 현재 비밀번호, 새로운 비밀번호, 새로운 비밀번호 확인을 포함하는 요청 객체
     * @return ResponseEntity 객체
     */
    ResponseEntity<Map<String, Object>> changePassword(String loginId, ChangePassword request);
}