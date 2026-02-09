package com.cloudia.backend.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash(value = "session", timeToLive = 604800)
public class UserSession {

    @Id
    private String loginId;        // ユーザーのログインID
    private String memberNumber;   // ユーザーの会員番号
    private String authorities;    // ユーザーの権限情報
    private String refreshToken;   // JWT リフレッシュトークン
    @Indexed
    private String accessToken;    // JWT アクセストークン
    @TimeToLive
    private Long expiration;       // セッションの有効期限（秒単位）
}
