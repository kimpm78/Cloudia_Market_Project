package com.cloudia.backend.CM_01_1010.service;

import org.springframework.http.ResponseEntity;
import java.util.Map;
import com.cloudia.backend.CM_01_1010.model.Unsubscribe;

public interface CM011010Service {

    /**
     * 사용자 회원탈퇴를 처리
     *
     * @param request 회원탈퇴 요청 데이터
     * @return 처리 결과와 메시지를 담은 ResponseEntity 객체
     */
    ResponseEntity<Map<String, Object>> unsubscribe(Unsubscribe request);
}
