package com.cloudia.backend.common.service;

/**
 * JWTの生成・検証およびRedisを用いたワンタイムトークン管理の共通サービス
 */
public interface JwtVerificationService {

    /**
     * 特定の識別子に対するJWTとRedisキーを生成
     *
     * @param identifier ユーザーを識別する一意の値
     * @return 生成されたJWT
     */
    String generateTokenAndKey(String identifier);

    /**
     * 渡されたJWTとRedisキーの有効性を検証
     *
     * @param token フロントエンドから受け取ったJWT
     * @param key   フロントエンドから受け取ったRedisキー
     * @return 有効な場合は識別子を返却、無効な場合はnullを返却
     */
    String verifyTokenAndKey(String token, String key);
}