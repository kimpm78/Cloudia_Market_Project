package com.cloudia.backend.CM_01_1003.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String newPassword;
    private String currentPassword;

}
