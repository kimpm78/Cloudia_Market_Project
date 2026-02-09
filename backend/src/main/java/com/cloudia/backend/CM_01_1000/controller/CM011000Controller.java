package com.cloudia.backend.CM_01_1000.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cloudia.backend.CM_01_1000.model.LoginRequest;
import com.cloudia.backend.auth.model.Token;
import com.cloudia.backend.auth.service.AuthService;
import com.cloudia.backend.common.model.ResponseModel;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CM011000Controller {

    private final AuthService authService;

    @Value("${jwt.cookie-domain:}")
    private String cookieDomain;

    @PostMapping("/guest/login")
    public ResponseEntity<ResponseModel<Token>> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

        Token token = authService.login(loginRequest.getLoginId(), loginRequest.getPassword());
        ResponseCookie refreshCookie = createRefreshTokenCookie(token.getRefreshToken());

        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(
                ResponseModel.<Token>builder()
                        .result(true)
                        .message("ログインに成功しました。")
                        .resultList(token)
                        .build());
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<ResponseModel<Token>> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        try {
            Token newToken = authService.refreshToken(refreshToken);

            ResponseCookie newRefreshCookie = createRefreshTokenCookie(newToken.getRefreshToken());

            response.addHeader("Set-Cookie", newRefreshCookie.toString());

            return ResponseEntity.ok(
                    ResponseModel.<Token>builder()
                            .result(true)
                            .message("トークンの更新に成功しました。")
                            .resultList(newToken)
                            .build());

        } catch (Exception e) {

            ResponseCookie finalDeleteCookie = ResponseCookie.from("refreshToken", "")
                    .path("/")
                    .maxAge(0)
                    .domain(cookieDomain.isEmpty() ? null : cookieDomain)
                    .build();

            response.addHeader("Set-Cookie", finalDeleteCookie.toString());

            return ResponseEntity.status(401).body(
                    ResponseModel.<Token>builder()
                            .result(false)
                            .message("トークンの有効期限が切れています。")
                            .build());
        }
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<ResponseModel<Void>> logout(HttpServletResponse response) {
        log.info("▶ [ログアウト要求]");

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .path("/")
                .maxAge(0)
                .domain(cookieDomain.isEmpty() ? null : cookieDomain)
                .build();

        response.addHeader("Set-Cookie", deleteCookie.toString());

        return ResponseEntity.ok(
                ResponseModel.<Void>builder()
                        .result(true)
                        .message("ログアウトに成功しました。")
                        .build());
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {

        boolean isServer = cookieDomain != null && !cookieDomain.isEmpty();

        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(isServer) // 開発/本番(HTTPS)はtrue、ローカルはfalse
                .path("/")
                .maxAge(60 * 60 * 24 * 7) // 7日
                .sameSite(isServer ? "None" : "Lax") // 開発/本番はNone(クロスサイト)、ローカルはLax
                .domain(isServer ? cookieDomain : null) // ★ 重要
                .build();
    }
}