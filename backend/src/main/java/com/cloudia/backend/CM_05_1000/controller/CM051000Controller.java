package com.cloudia.backend.CM_05_1000.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.DigestUtils;

import com.cloudia.backend.CM_05_1000.model.ResponseModel;
import com.cloudia.backend.CM_05_1000.model.NoticeInfo;
import com.cloudia.backend.CM_05_1000.service.CM051000Service;
import com.cloudia.backend.constants.CMMessageConstant;
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
@CrossOrigin(origins = "*")
public class CM051000Controller {
    // Service 정의
    private final CM051000Service cm051000Service;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 공지사항 전체 리스트 조회
     *
     * @return 공지사항 전체 목록
     */
    @GetMapping("/notice")
    public ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindAllBanner() {
        return cm051000Service.findByAllNotice();
    }
    /**
     * 공지사항 검색
     *
     * @param searchKeyword 검색 키워드
     * @param searchType    검색 유형 (1: 제목+내용, 2: 제목, 3: 내용)
     * @return 검색된 공지사항 리스트
     */
    @GetMapping("/notice/search")
    public ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindNotice(
            @RequestParam String searchKeyword,
            @RequestParam int searchType) {
        return cm051000Service.getFindNotice(searchKeyword, searchType);
    }


    /**
     * 특정 공지사항 + 이전/다음 공지사항 조회
     *
     * @param noticeId 공지사항 아이디
     * @return current, prev, next 포함된 공지사항 정보
     */
    @GetMapping("/notice/{noticeId}")
    public ResponseEntity<ResponseModel<java.util.Map<String, NoticeInfo>>> getFindIdNotice(@RequestParam int noticeId) {
        return cm051000Service.getFindIdNotice(noticeId);
    }

    /**
     * 공지사항 조회수 증가 (하루 1회 제한)
     *
     * @param noticeId 공지사항 ID
     * @return 처리 결과
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
     * 공지사항 등록
     *
     * @param entity         등록할 공지사항 정보
     * @param bindingResult  유효성 검증 결과
     * @return 등록 처리 결과
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
     * 공지사항 수정
     *
     * @param entity         수정할 공지사항 정보
     * @param bindingResult  유효성 검증 결과
     * @return 수정 처리 결과
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
     * 공지사항 삭제
     *
     * @param noticeId 삭제할 공지사항 ID
     * @param userId   요청자 ID
     * @return 삭제 처리 결과
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
     * 공통 응답 포맷 설정
     *
     * @param resultList 결과 데이터
     * @param ret        성공 여부
     * @param msg        메시지
     * @return 공통 응답 모델
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
