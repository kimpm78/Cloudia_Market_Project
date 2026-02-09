package com.cloudia.backend.CM_01_1009.controller;

import com.cloudia.backend.CM_01_1009.constants.CM011009MessageConstant;
import com.cloudia.backend.CM_01_1009.model.ChangePassword;
import com.cloudia.backend.CM_01_1009.service.CM011009Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Slf4j
@RequiredArgsConstructor
public class CM011009Controller {

    private final CM011009Service CM011009Service;

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody ChangePassword request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info(CM011009MessageConstant.CHANGE_PASSWORD_REQUEST_START);
        String loginId = userDetails.getUsername();
        ResponseEntity<Map<String, Object>> response = CM011009Service.changePassword(loginId, request);
        log.info(CM011009MessageConstant.CHANGE_PASSWORD_REQUEST_END);

        return response;
    }
}