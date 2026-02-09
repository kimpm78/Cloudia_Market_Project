package com.cloudia.backend.CM_01_1001.model;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequestModel {

    @Valid
    private User user;

    @Valid
    private Address address;
}