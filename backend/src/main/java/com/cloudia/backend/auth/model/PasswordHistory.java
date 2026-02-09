package com.cloudia.backend.auth.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class PasswordHistory {
    private Integer passwordHistoryId;   // パスワード履歴 id
    private String memberNumber;         // 会員番号
    private String password;             // パスワード
    private LocalDateTime createdAt;     // 作成日時
}