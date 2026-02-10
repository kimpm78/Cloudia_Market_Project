package com.cloudia.backend.CM_90_1000.service;

import java.util.List;

import com.cloudia.backend.CM_90_1000.model.Menu;

public interface CM901000Service {
    /*
     * サイドメニュー一覧を取得
     */
    List<Menu> findAllMenus();

    /*
     * メニュー一覧をツリー構造へ変換
     */
    List<Menu> buildMenuTree(List<Menu> flatList);
}