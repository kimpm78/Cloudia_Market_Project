package com.cloudia.backend.auth.model;

import com.cloudia.backend.CM_01_1001.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    private String accessToken;  // JWT アクセストークン
    private String refreshToken; // JWT リフレッシュトークン
    private User user;           // ユーザー情報
}
