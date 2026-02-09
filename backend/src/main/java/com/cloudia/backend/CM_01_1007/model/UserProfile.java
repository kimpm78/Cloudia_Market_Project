package com.cloudia.backend.CM_01_1007.model;

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
public class UserProfile {
    private String loginId;
    private String name;
    private String nationality;
    private String phoneNumber;
    private String postalCode;
    private String addressMain;
    private String addressDetail1;
    private String addressDetail2;
    private String addressDetail3;
    private String pccc;
}
