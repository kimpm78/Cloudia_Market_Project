package com.cloudia.backend.CM_01_1004.service.impl;

import com.cloudia.backend.CM_01_1004.mapper.CM011004Mapper;
import com.cloudia.backend.CM_01_1004.service.CM011004Service;
import com.cloudia.backend.CM_90_1000.model.Menu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM011004ServiceImpl implements CM011004Service {

    private final CM011004Mapper mypageMenuMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Menu> getMyPageMenus() {
        List<Menu> flatList = mypageMenuMapper.findMyPageMenus();
        return buildMenuTree(flatList);
    }

    private List<Menu> buildMenuTree(List<Menu> menuList) {
        Map<String, Menu> parentMenuMap = new LinkedHashMap<>();

        for (Menu menu : menuList) {
            if (menu.getMenuId().endsWith("0")) {
                parentMenuMap.put(menu.getMenuId(), menu);
            }
        }

        for (Menu menu : menuList) {
            if (!menu.getMenuId().endsWith("0")) {
                String parentId = menu.getMenuId().substring(0, 2) + "0";
                Menu parent = parentMenuMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(menu);
                }
            }
        }

        return parentMenuMap.values().stream()
                .sorted(Comparator.comparingInt(Menu::getSortOrder))
                .collect(Collectors.toList());
    }
}
