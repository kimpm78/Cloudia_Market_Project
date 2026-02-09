package com.cloudia.backend.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.auth.mapper.UserSessionRepository;
import com.cloudia.backend.auth.model.Token;
import com.cloudia.backend.auth.model.UserSession;
import com.cloudia.backend.config.jwt.JwtTokenProvider;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserSessionRepository userSessionRepository;
    private final UserDetailsService userDetailsService;

    /**
     * ログイン処理
     */
    @Transactional
    public Token login(String loginId, String password) {
        log.info("ログイン試行 (ID: {})", loginId);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginId, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Userオブジェクトを取得
        User user = (User) authentication.getPrincipal();

        // トークン生成
        String accessToken = jwtTokenProvider.createToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // Redisセッション保存
        String authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        UserSession session = UserSession.builder()
                .loginId(user.getLoginId())
                .memberNumber(user.getMemberNumber())
                .authorities(authorities)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiration(jwtTokenProvider.getRefreshTokenValidityInMilliseconds() / 1000) // 秒単位
                .build();

        userSessionRepository.save(session);
        log.info("Redisセッション保存完了 (ID: {})", user.getLoginId());

        user.setPassword(null);
        return new Token(accessToken, refreshToken, user);
    }

    /**
     * ログアウト処理
     */
    @Transactional
    public void logout(String loginId) {
        log.info("ログアウト処理 - Redisセッション削除 (ID: {})", loginId);
        // Redisに保存されているRefresh Token情報（UserSession）を削除します。
        userSessionRepository.deleteById(loginId);
    }

    /**
     * トークン更新
     */
    @Transactional
    public Token refreshToken(String refreshToken) {
        log.info("トークン更新開始");

        // トークン有効性チェック
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new JwtException("無効なRefresh Tokenです。");
        }

        // トークンからユーザーIDを抽出
        Authentication auth = jwtTokenProvider.getAuthentication(refreshToken);
        String loginId = auth.getName();

        // Redisからセッション取得
        UserSession session = userSessionRepository.findById(loginId)
                .orElseThrow(() -> new JwtException("セッションの有効期限が切れています。（Redis情報なし）"));

        // 受け取ったトークンとRedis上のトークンを比較
        if (!session.getRefreshToken().equals(refreshToken)) {
            throw new JwtException("トークン情報が一致しません。再ログインしてください。");
        }

        // ユーザーの最新情報をロード
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);
        Authentication newAuth = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        String newAccessToken = jwtTokenProvider.createToken(newAuth);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(newAuth);

        // Redisセッション更新
        session.setAccessToken(newAccessToken);
        session.setRefreshToken(newRefreshToken);
        userSessionRepository.save(session);
        log.info("Redisセッション更新完了");

        User user = (User) userDetails;
        user.setPassword(null);
        return new Token(newAccessToken, newRefreshToken, user);
    }
}