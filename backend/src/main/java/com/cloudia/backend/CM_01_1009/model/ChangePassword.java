package com.cloudia.backend.CM_01_1009.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePassword {
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;
}