package com.cloudia.backend.CM_02_1000.model;

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

public class HeaderMenu {
    private String menuId;      // メニュー ID
    private String menuName;    // メニュー名 
    private String url;         // URL
    private Integer sortOrder;  // 並び順
    private String icon;        // アイコン
}
