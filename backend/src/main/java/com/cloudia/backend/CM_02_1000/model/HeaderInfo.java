package com.cloudia.backend.CM_02_1000.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 헤더 영역에서 사용되는 정보를 담는 DTO 클래스입니다.
 * 메뉴 리스트와 장바구니 요약 정보를 포함합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HeaderInfo {
    private List<HeaderMenu> menus; // 헤더 메뉴 목록
    private Cart cart; // 장바구니 요약 정보
}
