package com.cloudia.backend.CM_90_1065.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.CM_90_1065.model.ProductCodeDto;
import com.cloudia.backend.CM_90_1065.service.CM901065Service;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.util.ResponseHelper;
import com.cloudia.backend.config.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin")
public class CM901065Controller {
    private final CM901065Service cm901065Service;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 상품 코드 전체 리스트 조회
     * 
     * @return 상품 코드 전체 리스트
     */
    @GetMapping("/productCode/getCode")
    public ResponseEntity<ResponseModel<List<ProductCodeDto>>> getCode() {
        List<ProductCodeDto> result = cm901065Service.getProductCode();
        return ResponseEntity.ok(ResponseHelper.success(result, "조회 성공"));
    }

    /**
     * 상품 코드 조회
     * 
     * @param searchTerm 키워드
     * @param searchType 타입 (1:상품 코드, 2:상품명)
     * @return 상품 코드 리스트
     */
    @GetMapping("/productCode/getFindCode")
    public ResponseEntity<ResponseModel<List<ProductCodeDto>>> getFindCode(@RequestParam String searchTerm,
            @RequestParam int searchType) {
        List<ProductCodeDto> result = cm901065Service.findByProductCode(searchTerm, searchType);
        return ResponseEntity.ok(ResponseHelper.success(result, "조회 성공"));
    }

    /**
     * 상품 코드 등록
     * 
     * @param res 상품 코드 정보
     */
    @PostMapping("/productCode/insCode")
    public ResponseEntity<ResponseModel<Integer>> insCode(@RequestBody ProductCodeDto entity,
            HttpServletRequest request) {
        String memberNumber = jwtTokenProvider.getMemberNoFromToken(jwtTokenProvider.resolveToken(request));
        Integer result = cm901065Service.insCode(entity, memberNumber);
        return ResponseEntity.ok(ResponseHelper.success(result, "업데이트 성공"));
    }

    /**
     * 상품 코드 삭제
     * 
     * @param res 상품 코드 정보
     */
    @PostMapping("/productCode/uptCode")
    public ResponseEntity<ResponseModel<Integer>> uptCode(@RequestBody List<ProductCodeDto> entity,
            HttpServletRequest request) {
        String memberNumber = jwtTokenProvider.getMemberNoFromToken(jwtTokenProvider.resolveToken(request));
        Integer result = cm901065Service.uptCode(entity, memberNumber);
        return ResponseEntity.ok(ResponseHelper.success(result, "업데이트 성공"));
    }
}
