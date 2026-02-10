package com.cloudia.backend.CM_90_1044.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_90_1044.model.NoticeInfo;
import com.cloudia.backend.CM_90_1044.service.CM901044Service;
import com.cloudia.backend.config.jwt.JwtTokenProvider;
import com.cloudia.backend.CM_90_1044.constants.CM901044MessageConstant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/menu")
public class CM901044Controller {
    private final CM901044Service cm901044Service;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * お知らせ全件一覧取得
     * 
     * @return お知らせ全件一覧
     */
    @GetMapping("/notice/findAll")
    public ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindAllBanner() {
        return cm901044Service.findByAllNotice();
    }

    /**
     * お知らせ検索（条件一覧取得）
     * 
     * @param searchKeyword キーワード
     * @param searchType    種別（1:タイトル＋本文、2:タイトル、3:本文）
     * @return お知らせ一覧
     */
    @GetMapping("/notice/findNotice")
    public ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindNotice(@RequestParam String searchKeyword,
            @RequestParam int searchType) {
        return cm901044Service.getFindNotice(searchKeyword, searchType);
    }

    /**
     * お知らせID指定取得
     * 
     * @param noticeId お知らせID
     * @return お知らせ一覧
     */
    @GetMapping("/notice/findIdNotice")
    public ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindIdNotice(@RequestParam int noticeId) {
        return cm901044Service.getFindIdNotice(noticeId);
    }

    /**
     * お知らせ更新
     * 
     * @param entity 更新対象のお知らせ情報
     * @return 更新結果
     */
    @PostMapping("/notice/update")
    public ResponseEntity<ResponseModel<Integer>> putNoticeUpdate(@Valid @RequestBody NoticeInfo entity,
            BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining("\r\n "));
            log.warn(CM901044MessageConstant.FAIL_NOTICE_VAL, errorMessage);
            return ResponseEntity.badRequest()
                    .body(setResponseDto(0, false, errorMessage));
        }
        String userId = jwtTokenProvider
                .getUserIdFromToken(jwtTokenProvider.resolveToken(request))
                .toString();
        return cm901044Service.noticeUpdate(entity, userId);
    }

    /**
     * お知らせ登録
     * 
     * @param entity 登録対象のお知らせ情報
     * @return 登録結果
     */
    @PostMapping("/notice/upload")
    public ResponseEntity<ResponseModel<Integer>> postNoticeUpload(@Valid @RequestBody NoticeInfo entity,
            BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining("\r\n "));
            log.warn(CM901044MessageConstant.FAIL_NOTICE_VAL, errorMessage);
            return ResponseEntity.badRequest()
                    .body(setResponseDto(0, false, errorMessage));
        }
        String userId = jwtTokenProvider
                .getUserIdFromToken(jwtTokenProvider.resolveToken(request))
                .toString();
        return cm901044Service.noticeUpload(entity, userId);
    }

    /**
     * ResponseModelの設定
     * 
     * @param resultList 一覧情報
     * @param ret        処理結果
     * @param msg        メッセージ
     * @return {@link ResponseModel} 一覧情報結果
     */
    private <T> ResponseModel<T> setResponseDto(T resultList, boolean ret, String msg) {
        return ResponseModel.<T>builder()
                .resultList(resultList)
                .result(ret)
                .message(msg)
                .build();
    }
}
