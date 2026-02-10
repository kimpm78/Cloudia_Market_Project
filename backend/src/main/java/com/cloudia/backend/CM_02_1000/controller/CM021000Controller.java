package com.cloudia.backend.CM_02_1000.controller;

import java.util.ArrayList;
import java.util.List;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_02_1000.model.HeaderMenu;
import com.cloudia.backend.CM_02_1000.service.CM021000Service;
import com.cloudia.backend.CM_02_1000.model.BannerInfo;
import com.cloudia.backend.CM_02_1000.constants.CM021000MessageConstant;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/guest")
public class CM021000Controller {

    // Service 定義
    @Autowired
    CM021000Service CM021000Service;

    /*
     * ヘッダーで使用するメニュー取得API
     * @return HeaderMenu リスト
     */
    @GetMapping("/menus")
    public ResponseEntity<List<HeaderMenu>> getHeaderMenusOnly() {
        log.info(CM021000MessageConstant.HEADER_MENU_START);
        List<HeaderMenu> menus = CM021000Service.findHeaderMenus();
        log.info(CM021000MessageConstant.HEADER_MENU_END);
        return ResponseEntity.ok(menus);
    }

    /**
     * ヘッダーに表示するアイコンメニュー（例：ログイン、マイページ、カート）
     * @return menu_code_value = 3 のメニューリスト
     */
    @GetMapping("/menus/icons")
    public ResponseEntity<List<HeaderMenu>> getHeaderIcons(
        @RequestParam(value = "roleId", required = false) Integer roleId
    ) {
        log.info(CM021000MessageConstant.HEADER_ICON_START);
        List<HeaderMenu> icons = new ArrayList<>(CM021000Service.findIconMenus());

        boolean isAdminOrManager = roleId != null && roleId <= 2;
        boolean hasAdminMenu = icons.stream().anyMatch(
            menu -> "/admin".equals(menu.getUrl()) || "管理者ページ".equals(menu.getMenuName())
        );

        if (isAdminOrManager && !hasAdminMenu) {
            icons.add(HeaderMenu.builder()
                .menuId("ADMIN")
                .menuName("管理者ページ")
                .url("/admin")
                .sortOrder(999)
                .icon("bi-gear")
                .build());
        }

        log.info(CM021000MessageConstant.HEADER_ICON_END);
        return ResponseEntity.ok(icons);
    }


    /**
     * ヘッダー用カート商品件数（バッジ表示用）
     * @param userId ユーザーID
     * @return カート内の商品件数
     */
    @GetMapping("/menus/cart/count")
    public ResponseEntity<Integer> getHeaderCartCount(@RequestParam("userId") Long userId) {
        int count = CM021000Service.getCartItemCount(userId);
        int totalAmount = CM021000Service.getCartTotalAmount(userId);
        log.info(CM021000MessageConstant.CART_GET_SUCCESS, count, totalAmount);
        return ResponseEntity.ok(count);
    }

    /**
     * バナー一覧取得
     * 
     * @return バナー一覧
     */
    @GetMapping("/menu/banner/findAll")
    public ResponseEntity<ResponseModel<List<BannerInfo>>> getFindAllBanner() {
        return CM021000Service.findByAllBanner();
    }
}
