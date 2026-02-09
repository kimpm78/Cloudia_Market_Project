package com.cloudia.backend.CM_01_1001.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_01_1001.model.SignUpRequestModel;

public interface CM011001UserService {

    /**
     * 새로운 사용자를 등록
     * 
     * @param request 사용자 등록 데이터
     * @return 등록된 SignUpRequestModel 객체
     */
    ResponseEntity<Map<String, Object>> signUp(SignUpRequestModel request);

    /**
     * 로그인 ID의 사용 가능 여부를 확인
     *
     * @param loginId 중복 여부를 확인할 로그인 ID
     * @return {@code ResponseEntity<Integer>}
     */

    ResponseEntity<Integer> checkLoginId(String loginId);

    /**
     * * 이메일 확인
     * * @param email 확인할 이메일 주소
     * 
     * @return 사용 가능하면 true, 이미 존재하면 false
     */
    boolean isEmailAvailable(String email);

    /**
     * 개인통관고유부호(PCCC)의 사용 가능 여부를 확인
     *
     * @param pccc 중복 여부를 확인할 PCCC 번호
     * @return {@code ResponseEntity<Integer>}
     */
    ResponseEntity<Integer> checkPccc(String pccc);

}