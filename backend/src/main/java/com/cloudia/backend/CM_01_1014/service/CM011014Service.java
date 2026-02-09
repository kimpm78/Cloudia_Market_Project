package com.cloudia.backend.CM_01_1014.service;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_01_1014.model.UserAccount;
import org.springframework.http.ResponseEntity;

public interface CM011014Service {
    // 조회 결과는 UserAccount 데이터가 필요함
    ResponseEntity<ResponseModel<UserAccount>> getAccount(String loginId);

    ResponseEntity<ResponseModel<Object>> updateAccount(String loginId, UserAccount accountDto);
}