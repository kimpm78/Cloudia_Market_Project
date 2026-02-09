package com.cloudia.backend.CM_01_1000.service;

import com.cloudia.backend.CM_01_1000.model.LoginRequest;
import org.springframework.http.ResponseEntity;
import java.util.Map;

public interface CM011000Service {
  /**
   * ユーザーを認証し、成功した場合はJWTを発行します。
   *
   * @param loginRequest ログインリクエストデータ（ID、パスワード）
   * @return ユーザー情報とJWTを含むレスポンス
   */
  ResponseEntity<Map<String, Object>> login(LoginRequest loginRequest);
}