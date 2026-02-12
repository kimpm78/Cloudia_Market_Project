package com.cloudia.backend.CM_01_1007.controller;

import com.cloudia.backend.CM_01_1007.constants.CM011007MessageConstant;
import com.cloudia.backend.CM_01_1007.model.UserProfile;
import com.cloudia.backend.CM_01_1007.service.CM011007Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class CM011007Controller {

    private final CM011007Service CM011007Service;

    /**
     * 現在ログイン中のユーザーのプロフィール情報を取得
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        String loginId = userDetails.getUsername();
        log.info(CM011007MessageConstant.CONTROLLER_GET_PROFILE_START, loginId);
        ResponseEntity<UserProfile> response = CM011007Service.getProfile(loginId);
        log.info(CM011007MessageConstant.CONTROLLER_GET_PROFILE_END, loginId);
        return response;
    }

    /**
     * 現在ログイン中のユーザーのプロフィール情報を更新
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserProfile profile) {
        String loginId = userDetails.getUsername();
        log.info(CM011007MessageConstant.CONTROLLER_UPDATE_PROFILE_START, loginId);
        ResponseEntity<Map<String, Object>> response = CM011007Service.updateProfile(loginId, profile);
        log.info(CM011007MessageConstant.CONTROLLER_UPDATE_PROFILE_END, loginId);
        return response;
    }
}