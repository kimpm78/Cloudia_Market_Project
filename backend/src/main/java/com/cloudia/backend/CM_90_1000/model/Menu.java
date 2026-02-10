package com.cloudia.backend.CM_90_1000.model;

import java.util.ArrayList;
import java.util.List;

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
public class Menu {
    private String menuId;                           // メニュー ID
    private String menuName;                         // メニュー名
    private String url;                              // 移動 URL
    private String parentId;                         // 上位メニュー ID
    private Integer sortOrder;                       // 並び順
    @Builder.Default
    private List<Menu> children = new ArrayList<>(); // 子メニューリスト
}
