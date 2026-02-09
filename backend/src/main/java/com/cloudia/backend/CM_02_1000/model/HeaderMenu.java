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
    private String menuId; // 메뉴 ID
    private String menuName; // 메뉴명 
    private String url; // URL
    private Integer sortOrder; // 정렬 순서
    private String icon; // 아이콘 클래스 (선택)
}
