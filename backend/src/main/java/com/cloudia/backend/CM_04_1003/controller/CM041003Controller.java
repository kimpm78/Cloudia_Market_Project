package com.cloudia.backend.CM_04_1003.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.config.jwt.JwtTokenProvider;
import com.cloudia.backend.CM_04_1003.constants.CM041003Constant;
import com.cloudia.backend.CM_04_1003.constants.CM041003MessageConstant;
import com.cloudia.backend.CM_04_1003.model.QnaAnswerRequest;
import com.cloudia.backend.CM_04_1003.model.QnaCreateRequest;
import com.cloudia.backend.CM_04_1003.model.QnaCreateResponse;
import com.cloudia.backend.CM_04_1003.model.QnaDetailResponse;
import com.cloudia.backend.CM_04_1003.model.QnaListResponse;
import com.cloudia.backend.CM_04_1003.model.QnaSummary;
import com.cloudia.backend.CM_04_1003.service.CM041003Service;
import com.cloudia.backend.common.model.ResponseModel;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class CM041003Controller {

    private final CM041003Service cm041003Service;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/guest/qna")
    public ResponseEntity<ResponseModel<QnaListResponse>> getQnaList(
            @RequestParam(name = "page", defaultValue = "" + CM041003Constant.DEFAULT_PAGE) int page,
            @RequestParam(name = "size", defaultValue = "" + CM041003Constant.DEFAULT_SIZE) int size,
            @RequestParam(name = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(name = "searchType", required = false) Integer searchType) {
        return cm041003Service.getQnaList(page, size, searchKeyword, searchType);
    }

    @GetMapping("/guest/qna/recent")
    public ResponseEntity<ResponseModel<java.util.List<QnaSummary>>> getRecentQna(
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "productId", required = false) String productId) {
        return cm041003Service.getRecentQna(size, productId);
    }

    @GetMapping("/guest/qna/{qnaId}")
    public ResponseEntity<ResponseModel<QnaDetailResponse>> getQnaDetail(
            @PathVariable("qnaId") Long qnaId,
            HttpServletRequest request) {
        Long requesterId = extractUserId(request);
        boolean isAdmin = hasAdminAuthority();
        return cm041003Service.getQnaDetail(qnaId, requesterId, isAdmin);
    }

    @PostMapping("/user/qna")
    public ResponseEntity<ResponseModel<QnaCreateResponse>> createQna(
            @Valid @RequestBody QnaCreateRequest request,
            BindingResult bindingResult) {
        if (hasAdminAuthority()) {
            ResponseModel<QnaCreateResponse> response = ResponseModel.<QnaCreateResponse>builder()
                    .result(false)
                    .message(CM041003MessageConstant.QNA_CREATE_FORBIDDEN)
                    .resultList(null)
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        if (bindingResult.hasErrors()) {
            String message = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse(CM041003MessageConstant.INVALID_REQUEST);
            ResponseModel<QnaCreateResponse> response = ResponseModel.<QnaCreateResponse>builder()
                    .result(false)
                    .message(message)
                    .resultList(null)
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
        return cm041003Service.createQna(request);
    }

    @PostMapping("/admin/qna/{qnaId}/answer")
    public ResponseEntity<ResponseModel<Void>> answerQna(
            @PathVariable("qnaId") Long qnaId,
            @Valid @RequestBody QnaAnswerRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse(CM041003MessageConstant.INVALID_REQUEST);
            return ResponseEntity.badRequest()
                    .body(buildSimpleResponse(false, message));
        }

        Long answererId = extractUserId(httpRequest);
        if (answererId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(buildSimpleResponse(false, CM041003MessageConstant.QNA_ANSWER_FORBIDDEN));
        }

        String loginId = extractLoginId();
        return cm041003Service.answerQna(qnaId, request, answererId, loginId);
    }

    @DeleteMapping("/user/qna/{qnaId}")
    public ResponseEntity<ResponseModel<Void>> deleteQna(
            @PathVariable("qnaId") Long qnaId,
            HttpServletRequest request) {
        Long requesterId = extractUserId(request);
        boolean isAdmin = hasAdminAuthority();
        return cm041003Service.deleteQna(qnaId, requesterId, isAdmin);
    }

    @DeleteMapping("/admin/qna/{qnaId}")
    public ResponseEntity<ResponseModel<Void>> deleteQnaAsAdmin(
            @PathVariable("qnaId") Long qnaId,
            HttpServletRequest request) {
        Long requesterId = extractUserId(request);
        // 管理者ルートでも同一ロジックを使用（adminフラグはtrue）
        return cm041003Service.deleteQna(qnaId, requesterId, true);
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return null;
        }
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    private String extractLoginId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    private boolean hasAdminAuthority() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> {
                    String authority = auth.getAuthority();
                    return "ROLE_ADMIN".equals(authority) || "ROLE_MANAGER".equals(authority);
                });
    }

    private ResponseModel<Void> buildSimpleResponse(boolean result, String message) {
        return ResponseModel.<Void>builder()
                .result(result)
                .message(message)
                .resultList(null)
                .build();
    }
}
