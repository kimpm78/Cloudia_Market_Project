package com.cloudia.backend.CM_01_1010.model;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Unsubscribe {

    private Integer userId;

    @NotBlank(message = "パスワードは必須項目です。")
    private String password;

    private List<String> reasons;
}
