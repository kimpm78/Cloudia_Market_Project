package com.cloudia.backend.CM_01_1000.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank
    private String loginId;  // ログインID
    @NotBlank
    private String password; // パスワード
}
