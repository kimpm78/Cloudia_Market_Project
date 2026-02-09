package com.cloudia.backend.config.jwt;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cloudia.backend.auth.mapper.UserSessionRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserSessionRepository userSessionRepository;
    private final UserDetailsService userDetailsService;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // ヘッダーからトークンを抽出
        String token = jwtTokenProvider.resolveToken(request);

        // トークン有効性チェック + Redisにセッションが存在するかの二次検証
        if (token != null && jwtTokenProvider.validateToken(token)) {

            boolean isSessionValid = userSessionRepository.findByAccessToken(token).isPresent();

            if (isSessionValid) {
                Authentication initialAuth = jwtTokenProvider.getAuthentication(token);
                String loginId = initialAuth.getName();

                try {
                    UserDetails principal = userDetailsService.loadUserByUsername(loginId);

                    if (principal != null) {
                        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

                        // 認証オブジェクト生成およびSecurityContextの設定
                        Authentication finalAuthentication = new UsernamePasswordAuthenticationToken(principal, token,
                                authorities);
                        SecurityContextHolder.getContext().setAuthentication(finalAuthentication);

                        log.info("SecurityContextの設定が完了しました: UserID={}", loginId);

                        // Redisセッションの延長
                        try {
                            String redisKey = "session:" + loginId;
                            // キーが存在する場合のみ延長
                            if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                                long refreshTokenTime = jwtTokenProvider.getRefreshTokenValidityInMilliseconds();
                                redisTemplate.expire(redisKey, refreshTokenTime, TimeUnit.MILLISECONDS);
                                log.debug("Redisセッション延長に成功 ({}ms): {}", refreshTokenTime, loginId);
                            }
                        } catch (Exception redisEx) {
                            log.warn("Redisセッション延長に失敗: {}", redisEx.getMessage());
                        }
                    }
                } catch (Exception e) {
                    log.error("ユーザー認証処理中にエラーが発生: {}", loginId, e);
                    SecurityContextHolder.clearContext();
                }
            }

        }
        filterChain.doFilter(request, response);
    }
}