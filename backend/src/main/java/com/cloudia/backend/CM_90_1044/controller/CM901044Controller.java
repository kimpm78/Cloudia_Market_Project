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

import com.cloudia.backend.CM_90_1044.model.ResponseModel;
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
    // Service 정의
    private final CM901044Service cm901044Service;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 공지사항 전체 리스트 조회
     * 
     * @return 공지사항 전체 리스트
     */
    @GetMapping("/notice/findAll")
    public ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindAllBanner() {
        return cm901044Service.findByAllNotice();
    }

    /**
     * 특정 공지사항 리스트 조회
     * 
     * @param searchKeyword 키워드
     * @param searchType    타입 (1:제목 + 내용, 2:제목, 3:내용)
     * @return 배너 리스트
     */
    @GetMapping("/notice/findNotice")
    public ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindNotice(@RequestParam String searchKeyword,
            @RequestParam int searchType) {
        return cm901044Service.getFindNotice(searchKeyword, searchType);
    }

    /**
     * 특정 공지사항 리스트 조회
     * 
     * @param noticeId 공지사항 아이디
     * @return 공지사항 리스트
     */
    @GetMapping("/notice/findIdNotice")
    public ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindIdNotice(@RequestParam int noticeId) {
        return cm901044Service.getFindIdNotice(noticeId);
    }

    /**
     * 공지사항 업데이트
     * 
     * @param entity 업데이트 할 공지사항 정보
     * @return 업데이트 여부
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
     * 공지사항 등록
     * 
     * @param entity 등록 할 공지사항 정보
     * @return 등록 여부
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
