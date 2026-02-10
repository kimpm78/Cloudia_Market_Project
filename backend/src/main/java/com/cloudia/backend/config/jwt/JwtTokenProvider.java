package com.cloudia.backend.config.jwt;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import static com.cloudia.backend.constants.JwtConstants.*;

import com.cloudia.backend.CM_01_1001.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Getter
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    private final JwtParser jwtParser;

    public JwtTokenProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.access-token-expiration}") long accessTokenValidityInMilliseconds,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenValidityInMilliseconds) {

        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds;
        this.jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    /**
     * Access Tokenを生成
     */
    public String createToken(Authentication authentication) {
        return buildToken(authentication, accessTokenValidityInMilliseconds);
    }

    /**
     * Refresh Tokenを生成
     */
    public String createRefreshToken(Authentication authentication) {
        return buildToken(authentication, refreshTokenValidityInMilliseconds);
    }

    /**
     * 共通トークン生成ロジック
     * Access TokenとRefresh Tokenの両方に権限情報を含める
     */
    private String buildToken(Authentication authentication, long validityDuration) {
        User user = (User) authentication.getPrincipal();

        // 権限情報を文字列へ変換
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityDuration);

        return Jwts.builder()
                .setSubject(user.getLoginId())
                .claim(KEY_USER_ID, user.getUserId())
                .claim(KEY_MEMBER_NO, user.getMemberNumber())
                .claim(KEY_ROLES, authorities)
                .claim(KEY_ROLE_ID, user.getRoleId())
                .claim(KEY_PERMISSION, user.getPermissionLevel())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key)
                .compact();
    }

    /**
     * トークンから認証情報を抽出
     */
    public Authentication getAuthentication(String token) {
        Claims claims = jwtParser.parseClaimsJws(token).getBody();

        Object authClaim = claims.get(KEY_ROLES);

        Collection<? extends GrantedAuthority> authorities;

        if (authClaim == null || authClaim.toString().trim().isEmpty()) {
            authorities = Collections.emptyList();
        } else {
            authorities = Arrays.stream(authClaim.toString().split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        User principal = User.builder()
                .loginId(claims.getSubject())
                .userId(claims.get(KEY_USER_ID, Integer.class))
                .memberNumber(claims.get(KEY_MEMBER_NO, String.class))
                .roleId(claims.get(KEY_ROLE_ID, Integer.class))
                .permissionLevel(claims.get(KEY_PERMISSION, String.class))
                .authorities(new ArrayList<>(authorities))
                .password("")
                .build();

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * JWTトークンからユーザーID(userId)を抽出（閲覧数加算、ログ記録など）
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = jwtParser.parseClaimsJws(token).getBody();
            Number userIdClaim = claims.get(KEY_USER_ID, Number.class);
            return userIdClaim != null ? userIdClaim.longValue() : null;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("トークンからuserIdの抽出に失敗: {}", e.getMessage());
            return null;
        }
    }

    public String getMemberNoFromToken(String token) {
        try {
            Claims claims = jwtParser.parseClaimsJws(token).getBody();
            String userMemberNoClaim = claims.get(KEY_MEMBER_NO, String.class);
            return userMemberNoClaim != null ? userMemberNoClaim.toString() : null;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("トークンからuserIdの抽出に失敗: {}", e.getMessage());
            return null;
        }
    }

    /**
     * トークンの有効性を検証
     */
    public boolean validateToken(String token) {
        try {
            jwtParser.parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("無効なJWTトークン: {}", e.getMessage());
            return false;
        }
    }

    /**
     * HTTPリクエストヘッダーからトークンを抽出
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
