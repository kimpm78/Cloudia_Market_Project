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
    private String menuId; // 메뉴 ID
    private String menuName; // 메뉴명
    private String url; // 이동 URL
    private String parentId; // 상위 메뉴 ID
    private Integer sortOrder; // 정렬 순서

    @Builder.Default
    private List<Menu> children = new ArrayList<>(); // 자식 메뉴 리스트
}
