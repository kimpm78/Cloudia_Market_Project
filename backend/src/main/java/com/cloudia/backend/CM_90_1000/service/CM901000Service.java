package com.cloudia.backend.CM_90_1000.service;

import java.util.List;

import com.cloudia.backend.CM_90_1000.model.Menu;

public interface CM901000Service {
    /*
     * 전체 사이드 메뉴 리스트 조회
     */
    List<Menu> findAllMenus();

    /*
     * 메뉴 리스트를 트리 구조로 변환
     */
    List<Menu> buildMenuTree(List<Menu> flatList);
}
