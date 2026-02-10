package com.cloudia.backend.CM_01_1014.service;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_01_1014.model.UserAccount;
import org.springframework.http.ResponseEntity;

public interface CM011014Service {
    // 取得結果には UserAccount データが必要
    ResponseEntity<ResponseModel<UserAccount>> getAccount(String loginId);

    ResponseEntity<ResponseModel<Object>> updateAccount(String loginId, UserAccount accountDto);
}