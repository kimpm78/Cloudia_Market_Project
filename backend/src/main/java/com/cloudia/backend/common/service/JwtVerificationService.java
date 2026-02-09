package com.cloudia.backend.common.service;

/**
 * JWT 생성, 검증 및 Redis를 사용한 일회용 토큰 관리를 위한 공통 서비스
 */
public interface JwtVerificationService {

    /**
     * 특정 식별자에 대한 JWT와 Redis 키를 생성
     * 
     * @param identifier 사용자를 식별하는 고유 값
     * @return 생성된 JWT
     */
    String generateTokenAndKey(String identifier);

    /**
     * 전달된 JWT와 Redis 키의 유효성을 검증
     * 
     * @param token 프론트엔드에서 전달받은 JWT
     * @param key   프론트엔드에서 전달받은 Redis 키
     * @return 유효하면 식별자를 반환, 아니면 null 반환
     */
    String verifyTokenAndKey(String token, String key);
}