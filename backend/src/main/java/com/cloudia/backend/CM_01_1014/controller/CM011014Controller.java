package com.cloudia.backend.CM_01_1014.controller;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_01_1014.constants.CM011014MessageConstant;
import com.cloudia.backend.CM_01_1014.model.UserAccount;
import com.cloudia.backend.CM_01_1014.service.CM011014Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class CM011014Controller {

    private final CM011014Service service;

    /**
     * 返金口座情報の取得
     */
    @GetMapping("/account")
    public ResponseEntity<ResponseModel<UserAccount>> getAccount(@AuthenticationPrincipal UserDetails userDetails) {
        String loginId = userDetails.getUsername();
        log.info(CM011014MessageConstant.CONTROLLER_GET_ACCOUNT_START, loginId);

        return service.getAccount(loginId);
    }

    /**
     * 返金口座情報の更新
     */
    @PutMapping("/account")
    public ResponseEntity<ResponseModel<Object>> updateAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserAccount accountDto) {
        String loginId = userDetails.getUsername();
        log.info(CM011014MessageConstant.CONTROLLER_UPDATE_ACCOUNT_START, loginId);

        return service.updateAccount(loginId, accountDto);
    }
}