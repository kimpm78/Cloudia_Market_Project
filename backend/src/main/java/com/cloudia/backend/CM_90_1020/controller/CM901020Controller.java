package com.cloudia.backend.CM_90_1020.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.CM_90_1020.model.UsersDto;
import com.cloudia.backend.CM_90_1020.service.CM901020Service;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.util.ResponseHelper;
import com.cloudia.backend.config.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class CM901020Controller {
    private final CM901020Service cm901020Service;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * ユーザー全件一覧を取得
     * 
     * @return ユーザー全件一覧
     */
    @GetMapping("/findAll")
    public ResponseEntity<ResponseModel<List<UsersDto>>> getFindAllUser() {
        List<UsersDto> users = cm901020Service.findByAllUsers();
        return ResponseEntity.ok(ResponseHelper.success(users, "取得に成功しました"));
    }

    /**
     * ユーザー検索
     * 
     * @param searchTerm キーワード
     * @param searchType タイプ（1:社員番号、2:ID）
     * @return ユーザー一覧
     */
    @GetMapping("/findUsers")
    public ResponseEntity<ResponseModel<List<UsersDto>>> getFindUsers(@RequestParam String searchTerm,
            @RequestParam int searchType) {
        List<UsersDto> users = cm901020Service.getFindUsers(searchTerm, searchType);
        return ResponseEntity.ok(ResponseHelper.success(users, "取得に成功しました"));
    }

    /**
     * 特定ユーザーを取得
     * 
     * @param memberId 社員番号
     * @return ユーザー情報
     */
    @GetMapping("/findUser")
    public ResponseEntity<ResponseModel<UsersDto>> getFindUser(@RequestParam String memberId) {
        UsersDto user = cm901020Service.getFindUser(memberId);
        return ResponseEntity.ok(ResponseHelper.success(user, "取得に成功しました"));
    }

    /**
     * ユーザー更新
     * 
     * @param userInfo ユーザー情報
     * @return 更新結果
     */
    @PostMapping("/update")
    public ResponseEntity<ResponseModel<Integer>> postUserUpdate(@RequestBody UsersDto entity,
            HttpServletRequest request) {
        String userId = jwtTokenProvider
                .getUserIdFromToken(jwtTokenProvider.resolveToken(request))
                .toString();
        Integer result = cm901020Service.postUserUpdate(entity, userId);
        return ResponseEntity.ok(ResponseHelper.success(result, "更新に成功しました"));
    }
}
