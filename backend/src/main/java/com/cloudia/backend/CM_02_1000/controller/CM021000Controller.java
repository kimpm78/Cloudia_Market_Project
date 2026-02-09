package com.cloudia.backend.CM_02_1000.controller;

import java.util.ArrayList;
import java.util.List;

import com.cloudia.backend.CM_02_1000.model.HeaderMenu;
import com.cloudia.backend.CM_02_1000.service.CM021000Service;
import com.cloudia.backend.CM_02_1000.model.BannerInfo;
import com.cloudia.backend.CM_02_1000.model.ResponseModel;
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
@CrossOrigin(origins = "*") // 개발 단계에서만 * 허용
public class CM021000Controller {

    // Service 정의
    @Autowired
    CM021000Service CM021000Service;

    /*
     * 헤더에서 사용할 메뉴 + 장바구니 정보 통합 API
     * @return HeaderInfo DTO (menus, cart)
     */
    @GetMapping("/menus")
    public ResponseEntity<List<HeaderMenu>> getHeaderMenusOnly() {
        log.info(CM021000MessageConstant.HEADER_MENU_START);
        List<HeaderMenu> menus = CM021000Service.findHeaderMenus();
        log.info(CM021000MessageConstant.HEADER_MENU_END);
        return ResponseEntity.ok(menus);
    }

    /**
     * 헤더에 표시할 아이콘 메뉴 (ex. 로그인, 마이페이지, 장바구니)
     * @return menu_code_value = 3 인 메뉴 리스트
     */
    @GetMapping("/menus/icons")
    public ResponseEntity<List<HeaderMenu>> getHeaderIcons(
        @RequestParam(value = "roleId", required = false) Integer roleId
    ) {
        log.info(CM021000MessageConstant.HEADER_ICON_START);
        List<HeaderMenu> icons = new ArrayList<>(CM021000Service.findIconMenus());

        boolean isAdminOrManager = roleId != null && roleId <= 2;
        boolean hasAdminMenu = icons.stream().anyMatch(
            menu -> "/admin".equals(menu.getUrl()) || "관리자 페이지".equals(menu.getMenuName())
        );

        if (isAdminOrManager && !hasAdminMenu) {
            icons.add(HeaderMenu.builder()
                .menuId("ADMIN")
                .menuName("관리자 페이지")
                .url("/admin")
                .sortOrder(999)
                .icon("bi-gear")
                .build());
        }

        log.info(CM021000MessageConstant.HEADER_ICON_END);
        return ResponseEntity.ok(icons);
    }


    /**
     * 헤더용 장바구니 아이템 카운트 (배지 표시용)
     * @param userId 사용자 ID
     * @return 장바구니 담긴 상품 개수
     */
    @GetMapping("/menus/cart/count")
    public ResponseEntity<Integer> getHeaderCartCount(@RequestParam("userId") Long userId) {
        int count = CM021000Service.getCartItemCount(userId);
        int totalAmount = CM021000Service.getCartTotalAmount(userId);
        log.info(CM021000MessageConstant.CART_GET_SUCCESS, count, totalAmount);
        return ResponseEntity.ok(count);
    }

    /**
     * 배너 전체 리스트 조회
     * 
     * @return 배너 전체 리스트
     */
    @GetMapping("/menu/banner/findAll")
    public ResponseEntity<ResponseModel<List<BannerInfo>>> getFindAllBanner() {
        return CM021000Service.findByAllBanner();
    }
}
