package com.cloudia.backend.CM_90_1000.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.CM_90_1000.model.Menu;
import com.cloudia.backend.CM_90_1000.service.CM901000Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController("CM_90_1000")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin")
public class CM901000Controller {

    @Autowired
    private final CM901000Service cm901000Service;

    /*
    * サイドメニュー全件一覧取得
    *
    * @return サイドメニュー一覧
    */
    @GetMapping("/menu/all")
    public ResponseEntity<List<Menu>> findAllMenus() {
        log.info("サイドメニュー全件一覧取得 Start");
        List<Menu> menuList = cm901000Service.findAllMenus();
        log.info("サイドメニュー全件一覧取得 End");
        return ResponseEntity.ok(menuList);
    }

}
