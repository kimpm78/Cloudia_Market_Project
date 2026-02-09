package com.cloudia.backend.common.service.impl;

import com.cloudia.backend.common.service.JwtVerificationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class JwtVerificationServiceImpl implements JwtVerificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SecretKey secretKey;

    private static final String KEY_PREFIX = "temp-auth-key:";
    private static final long EXPIRATION_MINUTES = 3;

    public JwtVerificationServiceImpl(RedisTemplate<String, String> redisTemplate,
            @Value("${jwt.secret-key}") String secret) {
        this.redisTemplate = redisTemplate;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateTokenAndKey(String identifier) {
        String key = UUID.randomUUID().toString();
        String redisKey = KEY_PREFIX + identifier;
        redisTemplate.opsForValue().set(redisKey, key, EXPIRATION_MINUTES, TimeUnit.MINUTES);

        return Jwts.builder()
                .claim("identifier", identifier)
                .claim("key", key)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String verifyTokenAndKey(String token, String key) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String identifier = claims.get("identifier", String.class);
            String keyFromToken = claims.get("key", String.class);

            // JWT에 포함된 키와 파라미터로 받은 키가 일치하는지 확인
            if (key == null || !key.equals(keyFromToken)) {
                return null;
            }

            // Redis에 저장된 키와 일치하는지 확인
            String redisKey = KEY_PREFIX + identifier;
            String storedKey = redisTemplate.opsForValue().get(redisKey);

            if (storedKey != null && storedKey.equals(key)) {
                redisTemplate.delete(redisKey);
                return identifier;
            }

            return null;
        } catch (Exception e) {
            // JWT 파싱 실패 또는 유효하지 않은 토큰
            return null;
        }
    }
}
