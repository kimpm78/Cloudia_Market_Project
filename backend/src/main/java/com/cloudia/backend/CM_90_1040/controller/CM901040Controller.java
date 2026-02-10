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

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_90_1040.model.BannerInfo;
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
    private final CM901040Service cm901040Service;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * バナー全件一覧取得
     * 
     * @return バナー全件一覧
     */
    @GetMapping("/banner/findAll")
    public ResponseEntity<ResponseModel<List<BannerInfo>>> getFindAllBanner() {
        return cm901040Service.findByAllBanner();
    }

    /**
     * 特定バナー一覧取得
     * 
     * @param searchTerm バナー名
     * @return バナー一覧
     */
    @GetMapping("/banner/findBanner")
    public ResponseEntity<ResponseModel<List<BannerInfo>>> getFindBanner(@RequestParam String searchTerm) {
        return cm901040Service.findByBanner(searchTerm);
    }

    /**
     * 更新対象バナー取得
     * 
     * @param bannerId バナーID
     * @return バナー情報
     */
    @GetMapping("/banner/findIdBanner")
    public ResponseEntity<ResponseModel<BannerInfo>> getFindBanner(@RequestParam int bannerId) {
        return cm901040Service.findByBanner(bannerId);
    }

    /**
     * バナー削除
     * 
     * @param entity 削除対象バナー一覧
     * @return 削除結果
     */
    @DeleteMapping("/banner/del")
    public ResponseEntity<ResponseModel<Integer>> BannerDel(@RequestBody List<BannerInfo> entity) {
        return cm901040Service.bannerDel(entity);
    }

    /**
     * バナー登録
     * 
     * @param entity 登録するバナー情報
     * @return 登録結果
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
     * バナー更新
     * 
     * @param entity 更新するバナー情報
     * @return 更新結果
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
     * 使用可能な表示順取得
     * 
     * @return 使用可能な表示順一覧
     */
    @GetMapping("/banner/availableDisplayOrders")
    public ResponseEntity<ResponseModel<List<Integer>>> getFindDisplayOrder() {
        return cm901040Service.getFindDisplayOrder();
    }

    /**
     * ResponseModelを設定
     * 
     * @param resultList 結果データ
     * @param ret        処理結果
     * @param msg        メッセージ
     * @return {@link ResponseModel} 結果データ
     */
    private <T> ResponseModel<T> setResponseDto(T resultList, boolean ret, String msg) {
        return ResponseModel.<T>builder()
                .resultList(resultList)
                .result(ret)
                .message(msg)
                .build();
    }
}
