package com.cloudia.backend.CM_90_1043.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_90_1043.model.CategorySaveRequest;
import com.cloudia.backend.CM_90_1043.service.CM901043Service;
import com.cloudia.backend.config.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/category")
public class CM901043Controller {
    private final CM901043Service cm901043Service;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * カテゴリ照会
     */
    @GetMapping("/findByAllCategory")
    public ResponseEntity<ResponseModel<Map<String, Object>>> findByAllCategory() {
        return cm901043Service.findByAllCategory();
    }

    /**
     * カテゴリ変更内容の保存（追加/更新/削除）
     */
    @PostMapping("/save")
    public ResponseEntity<ResponseModel<String>> saveCategory(@RequestBody CategorySaveRequest requests,
            HttpServletRequest request) {
        String userId = jwtTokenProvider
                .getUserIdFromToken(jwtTokenProvider.resolveToken(request))
                .toString();
        return cm901043Service.saveChanges(requests, userId);
    }
}
