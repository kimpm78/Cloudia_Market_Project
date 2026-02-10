package com.cloudia.backend.CM_01_1010.service;

import org.springframework.http.ResponseEntity;
import java.util.Map;
import com.cloudia.backend.CM_01_1010.model.Unsubscribe;

public interface CM011010Service {

    /**
     * ユーザーの退会処理を行う
     *
     * @param request 退会リクエストデータ
     * @return 処理結果とメッセージを含む ResponseEntity オブジェクト
     */
    ResponseEntity<Map<String, Object>> unsubscribe(Unsubscribe request);
}
