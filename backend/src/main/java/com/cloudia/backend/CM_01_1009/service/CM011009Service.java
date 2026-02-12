package com.cloudia.backend.CM_01_1009.service;

import com.cloudia.backend.CM_01_1009.model.ChangePassword;
import org.springframework.http.ResponseEntity;
import java.util.Map;

public interface CM011009Service {
    /**
     * @param loginId 変更をリクエストしたユーザーのログインID
     * @param request 現在のパスワード、新しいパスワード、新しいパスワード確認を含むリクエストオブジェクト
     * @return ResponseEntityオブジェクト
     */
    ResponseEntity<Map<String, Object>> changePassword(String loginId, ChangePassword request);
}