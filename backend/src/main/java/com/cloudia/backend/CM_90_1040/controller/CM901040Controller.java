package com.cloudia.backend.CM_90_1040.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.CM_90_1040.model.BannerInfo;
import com.cloudia.backend.CM_90_1040.model.ResponseModel;
import com.cloudia.backend.CM_90_1040.service.CM901040Service;
import com.cloudia.backend.config.jwt.JwtTokenProvider;
import com.cloudia.backend.CM_90_1040.constants.CM901040MessageConstant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/menu")
public class CM901040Controller {
    // Service 정의
    private final CM901040Service cm901040Service;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 배너 전체 리스트 조회
     * 
     * @return 배너 전체 리스트
     */
    @GetMapping("/banner/findAll")
    public ResponseEntity<ResponseModel<List<BannerInfo>>> getFindAllBanner() {
        return cm901040Service.findByAllBanner();
    }

    /**
     * 특정 배너 리스트 조회
     * 
     * @param searchTerm 배너명
     * @return 배너 리스트
     */
    @GetMapping("/banner/findBanner")
    public ResponseEntity<ResponseModel<List<BannerInfo>>> getFindBanner(@RequestParam String searchTerm) {
        return cm901040Service.findByBanner(searchTerm);
    }

    /**
     * 업데이트 배너 리스트 조회
     * 
     * @param bannerId 배너 아이디
     * @return 배너 리스트
     */
    @GetMapping("/banner/findIdBanner")
    public ResponseEntity<ResponseModel<BannerInfo>> getFindBanner(@RequestParam int bannerId) {
        return cm901040Service.findByBanner(bannerId);
    }

    /**
     * 배너 삭제
     * 
     * @param entity 배너 삭제 항목 리스트
     * @return 삭제 여부
     */
    @DeleteMapping("/banner/del")
    public ResponseEntity<ResponseModel<Integer>> BannerDel(@RequestBody List<BannerInfo> entity) {
        return cm901040Service.bannerDel(entity);
    }

    /**
     * 배너 등록
     * 
     * @param entity 등록 할 배너 정보
     * @return 등록 여부
     */
    @PostMapping("/banner/upload")
    public ResponseEntity<ResponseModel<Integer>> postBannerUpload(@Valid @ModelAttribute BannerInfo entity,
            BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining("\r\n "));
            log.warn(CM901040MessageConstant.FAIL_BANNER_VAL, errorMessage);
            return ResponseEntity.badRequest()
                    .body(setResponseDto(0, false, errorMessage));
        }
        String userId = jwtTokenProvider
                .getUserIdFromToken(jwtTokenProvider.resolveToken(request))
                .toString();
        return cm901040Service.bannerUpload(entity, userId);
    }

    /**
     * 배너 업데이트
     * 
     * @param entity 업데이트 할 배너 정보
     * @return 업데이트 여부
     */
    @PostMapping("/banner/update")
    public ResponseEntity<ResponseModel<Integer>> postBannerUpdate(@Valid @ModelAttribute BannerInfo entity,
            BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining("\r\n "));
            log.warn(CM901040MessageConstant.FAIL_BANNER_VAL, errorMessage);
            return ResponseEntity.badRequest()
                    .body(setResponseDto(0, false, errorMessage));
        }
        String userId = jwtTokenProvider
                .getUserIdFromToken(jwtTokenProvider.resolveToken(request))
                .toString();
        return cm901040Service.bannerUpdate(entity, userId);
    }

    /**
     * 배너 전체 리스트 조회
     * 
     * @return 배너 전체 리스트
     */
    @GetMapping("/banner/availableDisplayOrders")
    public ResponseEntity<ResponseModel<List<Integer>>> getFindDisplayOrder() {
        return cm901040Service.getFindDisplayOrder();
    }

    /**
     * ResponseModel을 셋팅
     * 
     * @param resultList 리스트 정보
     * @param ret        처리 결과
     * @param msg        메시지
     * @return {@link ResponseModel} 리스트 정보 결과
     */
    private <T> ResponseModel<T> setResponseDto(T resultList, boolean ret, String msg) {
        return ResponseModel.<T>builder()
                .resultList(resultList)
                .result(ret)
                .message(msg)
                .build();
    }
}
