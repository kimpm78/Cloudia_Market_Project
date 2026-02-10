package com.cloudia.backend.CM_90_1000.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_90_1000.constants.CM0901000MessageConstant;
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

    /**
     * サイドメニュー一覧を取得
     * 
     * @return サイドメニュー一覧
     */
    @Override
    @Transactional(readOnly = true)
    public List<Menu> findAllMenus() {
        try {
            List<Menu> sideMenus = cm901000Mapper.findAllMenus();
            log.info(CM0901000MessageConstant.LOG_MENU_FETCHED_COUNT, sideMenus.size());
            return buildMenuTree(sideMenus);
        } catch (Exception e) {
            log.error(CM0901000MessageConstant.LOG_MENU_FIND_ALL_ERROR, e);
            return List.of();
        }
    }

    /**
     * メニュー一覧をツリー構造へ変換
     * 
     * @param 取得したメニュー一覧
     * @return サイドメニューのツリー構造一覧
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

        // 子メニュー連結
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

        // 結果ツリー生成
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
