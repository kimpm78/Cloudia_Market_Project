package com.cloudia.backend.CM_90_1000.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_90_1000.mapper.CM901000Mapper;
import com.cloudia.backend.CM_90_1000.model.Menu;
import com.cloudia.backend.CM_90_1000.service.CM901000Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM901000ServiceImpl implements CM901000Service {

    private final CM901000Mapper cm901000Mapper;

    /*
     * 전체 사이드 메뉴 리스트 조회
     * 
     * @return 사이드 메뉴 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<Menu> findAllMenus() {
        try {
            List<Menu> sideMenus = cm901000Mapper.findAllMenus();
            log.info("조회된 메뉴 수: {}", sideMenus.size());
            return buildMenuTree(sideMenus);
        } catch (Exception e) {
            log.error("사이드 메뉴 조회 중 오류 발생", e);
            return null;
        }
    }

    /*
     * 메뉴 리스트를 트리 구조로 변환
     * 
     * @param 전체 조회된 메뉴 리스트
     * 
     * @return 사이드 메뉴 트리 구조 리스트
     */
    @Override
    public List<Menu> buildMenuTree(List<Menu> menuList) {
        Map<String, Menu> parentMap = new LinkedHashMap<>();
        List<Menu> resultTree = new ArrayList<>();

        for (Menu menu : menuList) {
            String menuId = menu.getMenuId();
            if (menuId.endsWith("0")) {
                parentMap.put(menuId, menu);
            }
        }

        // 하위 메뉴 연결
        for (Menu menu : menuList) {
            String menuId = menu.getMenuId();
            if (!menuId.endsWith("0")) {
                String parentKey = menuId.substring(0, menuId.length() - 1) + "0";
                Menu parent = parentMap.get(parentKey);
                if (parent != null) {
                    parent.getChildren().add(menu);
                }
            }
        }

        // 결과 트리 만들기
        resultTree = parentMap.values().stream()
                .sorted(Comparator.comparingInt(Menu::getSortOrder))
                .peek(parent -> parent.setChildren(
                        parent.getChildren().stream()
                                .sorted(Comparator.comparingInt(Menu::getSortOrder))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());

        return resultTree;
    }
}
