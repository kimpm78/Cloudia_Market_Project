package com.cloudia.backend.CM_01_1004.controller;

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
     * 마이페이지 사이드바 메뉴 목록을 조회
     */
    @GetMapping("/mypage/menus")
    public ResponseEntity<List<Menu>> getMyPageMenus() {
        log.info("마이페이지 메뉴 목록 조회 요청 시작");
        List<Menu> menuList = mypageMenuService.getMyPageMenus();
        log.info("마이페이지 메뉴 목록 조회 요청 종료");
        return ResponseEntity.ok(menuList);
    }
}