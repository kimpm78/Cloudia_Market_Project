package com.cloudia.backend.auth.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Role {
    private Integer roleId;   // ロールID
    private String roleType;  // ロールタイプ
    private String roleName;  // ロール名
    private Integer isActive; // 有効フラグ
}