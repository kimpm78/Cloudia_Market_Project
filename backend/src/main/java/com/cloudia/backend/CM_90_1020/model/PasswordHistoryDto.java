package com.cloudia.backend.CM_90_1020.model;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PasswordHistoryDto {
    private Integer userId;             // 主キー
    private String memberNumber;        // 社員番号
    private String password;            // パスワード
    private OffsetDateTime createdAt;   // 作成日
}