package com.cloudia.backend.CM_01_1004.controller;

import com.cloudia.backend.CM_01_1004.constants.CM011004MessageConstant;
import com.cloudia.backend.CM_01_1004.service.CM011004Service;
import com.cloudia.backend.CM_90_1000.model.Menu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/user")
public class CM011004Controller {

    private final CM011004Service mypageMenuService;

    /**
     * マイページのサイドバーメニュー一覧を取得
     */
    @GetMapping("/mypage/menus")
    public ResponseEntity<List<Menu>> getMyPageMenus() {
        log.info(CM011004MessageConstant.MYPAGE_MENU_REQUEST_START);
        List<Menu> menuList = mypageMenuService.getMyPageMenus();
        log.info(CM011004MessageConstant.MYPAGE_MENU_REQUEST_END);
        return ResponseEntity.ok(menuList);
    }
}