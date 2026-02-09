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
     * 유저 전체 리스트 조회
     * 
     * @return 유저 전체 리스트
     */
    @GetMapping("/findAll")
    public ResponseEntity<ResponseModel<List<UsersDto>>> getFindAllUser() {
        List<UsersDto> users = cm901020Service.findByAllUsers();
        return ResponseEntity.ok(ResponseHelper.success(users, "조회 성공"));
    }

    /**
     * 유저 조회
     * 
     * @param searchTerm 키워드
     * @param searchType 타입 (1:사원 번호, 2:ID)
     * @return 유저 리스트
     */
    @GetMapping("/findUsers")
    public ResponseEntity<ResponseModel<List<UsersDto>>> getFindUsers(@RequestParam String searchTerm,
            @RequestParam int searchType) {
        List<UsersDto> users = cm901020Service.getFindUsers(searchTerm, searchType);
        return ResponseEntity.ok(ResponseHelper.success(users, "조회 성공"));
    }

    /**
     * 특정 유저 조회
     * 
     * @param memberId 사원 번호
     * @return 유저 리스트
     */
    @GetMapping("/findUser")
    public ResponseEntity<ResponseModel<UsersDto>> getFindUser(@RequestParam String memberId) {
        UsersDto user = cm901020Service.getFindUser(memberId);
        return ResponseEntity.ok(ResponseHelper.success(user, "조회 성공"));
    }

    /**
     * 유저 업데이트
     * 
     * @param userInfo 유저 정보
     * @return 성공 여부
     */
    @PostMapping("/update")
    public ResponseEntity<ResponseModel<Integer>> postUserUpdate(@RequestBody UsersDto entity,
            HttpServletRequest request) {
        String userId = jwtTokenProvider
                .getUserIdFromToken(jwtTokenProvider.resolveToken(request))
                .toString();
        Integer result = cm901020Service.postUserUpdate(entity, userId);
        return ResponseEntity.ok(ResponseHelper.success(result, "업데이트 성공"));
    }
}
