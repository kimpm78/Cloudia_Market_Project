package com.cloudia.backend.CM_05_1000.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.DigestUtils;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.constants.CMMessageConstant;
import com.cloudia.backend.CM_05_1000.model.NoticeInfo;
import com.cloudia.backend.CM_05_1000.service.CM051000Service;
import com.cloudia.backend.CM_05_1000.constants.CM051000MessageConstant;
import com.cloudia.backend.config.jwt.JwtTokenProvider;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/guest")
public class CM051000Controller {
    private final CM051000Service cm051000Service;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * お知らせ一覧（全件）取得
     *
     * @return お知らせの全件一覧
     */
    @GetMapping("/notice")
    public ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindAllBanner() {
        return cm051000Service.findByAllNotice();
    }
    /**
     * お知らせ検索
     *
     * @param searchKeyword 検索キーワード
     * @param searchType    検索タイプ (1: タイトル+内容, 2: タイトル, 3: 内容)
     * @return 検索されたお知らせリスト
     */
    @GetMapping("/notice/search")
    public ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindNotice(
            @RequestParam String searchKeyword,
            @RequestParam int searchType) {
        return cm051000Service.getFindNotice(searchKeyword, searchType);
    }

    /**
     * 特定のお知らせ + 前後（前/次）のお知らせ取得
     *
     * @param noticeId お知らせID
     * @return current, prev, next を含むお知らせ情報
     */
    @GetMapping("/notice/{noticeId}")
    public ResponseEntity<ResponseModel<java.util.Map<String, NoticeInfo>>> getFindIdNotice(@RequestParam int noticeId) {
        return cm051000Service.getFindIdNotice(noticeId);
    }

    /**
     * お知らせ閲覧数の増加（1日1回まで）
     *
     * @param noticeId お知らせID
     * @return 処理結果
     */
    @PostMapping("/notice/{noticeId}/view")
    public ResponseEntity<ResponseModel<Boolean>> increaseViewCount(
            @PathVariable int noticeId,
            HttpServletRequest request) {
        try {
            String viewerKey = buildViewerKey(request);
            boolean incremented = cm051000Service.increaseViewOncePerDay(noticeId, viewerKey);

            String message = incremented
                ? CM051000MessageConstant.NOTICE_VIEW_INCREMENT_SUCCESS
                : CM051000MessageConstant.NOTICE_VIEW_ALREADY_COUNTED;
            return ResponseEntity.ok(setResponseDto(Boolean.valueOf(incremented), true, message));
        } catch (Exception e) {
            log.error("{} noticeId: {}", CM051000MessageConstant.NOTICE_VIEW_INCREMENT_ERROR, noticeId, e);
            return ResponseEntity.status(500)
                .body(setResponseDto(Boolean.FALSE, false, CM051000MessageConstant.NOTICE_VIEW_INCREMENT_FAIL));
        }
    }

    /**
     * お知らせ登録
     *
     * @param entity        登録するお知らせ情報
     * @param bindingResult バリデーション結果
     * @return 登録処理結果
     */
    @PostMapping("/notice/upload")
    public ResponseEntity<ResponseModel<Integer>> postNoticeUpload(@Valid @RequestBody NoticeInfo entity,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining("\r\n "));
            log.warn(CM051000MessageConstant.FAIL_NOTICE_VAL, errorMessage);
            return ResponseEntity.badRequest()
                    .body(setResponseDto(0, false, errorMessage));
        }
        if (!"1".equals(String.valueOf(entity.getUserId()))) {
            return ResponseEntity.status(403)
                    .body(setResponseDto(0, false, CMMessageConstant.FAIL_UNAUTHORIZED));
        }
        return cm051000Service.noticeUpload(entity);
    }

    /**
     * お知らせ更新
     *
     * @param entity        更新するお知らせ情報
     * @param bindingResult バリデーション結果
     * @return 更新処理結果
     */
    @PostMapping("/notice/update")
    public ResponseEntity<ResponseModel<Integer>> putNoticeUpdate(@Valid @RequestBody NoticeInfo entity,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining("\r\n "));
            log.warn(CM051000MessageConstant.FAIL_NOTICE_VAL, errorMessage);
            return ResponseEntity.badRequest()
                    .body(setResponseDto(0, false, errorMessage));
        }
        if (!"1".equals(String.valueOf(entity.getUserId()))) {
            return ResponseEntity.status(403)
                    .body(setResponseDto(0, false, CMMessageConstant.FAIL_UNAUTHORIZED));
        }
        return cm051000Service.noticeUpdate(entity);
    }

    /**
     * お知らせ削除
     *
     * @param noticeId 削除するお知らせID
     * @param userId   リクエストユーザーID
     * @return 削除処理結果
     */
    @DeleteMapping("/notice/{noticeId}")
    public ResponseEntity<ResponseModel<Integer>> deleteNotice(
            @PathVariable Long noticeId,
            @RequestParam String userId) {
        if (!"1".equals(String.valueOf(userId))) {
            return ResponseEntity.status(403)
                    .body(setResponseDto(0, false, CMMessageConstant.FAIL_UNAUTHORIZED));
        }
        return cm051000Service.deleteNotice(noticeId);
    }

    /**
     * 共通レスポンス形式の設定
     *
     * @param resultList 結果データ
     * @param ret        成否
     * @param msg        メッセージ
     * @return 共通レスポンスモデル
     */
    private <T> ResponseModel<T> setResponseDto(T resultList, boolean ret, String msg) {
        return ResponseModel.<T>builder()
                .resultList(resultList)
                .result(ret)
                .message(msg)
                .build();
    }

    private String buildViewerKey(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            if (userId != null) {
                return "user:" + userId;
            }
        }
        String clientIp = resolveClientIp(request);
        String hashedIp = DigestUtils.md5DigestAsHex(clientIp.getBytes(StandardCharsets.UTF_8));
        return "guest:" + hashedIp;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
