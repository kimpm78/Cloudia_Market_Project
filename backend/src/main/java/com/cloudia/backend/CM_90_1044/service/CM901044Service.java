package com.cloudia.backend.CM_90_1044.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_90_1044.model.NoticeInfo;
import com.cloudia.backend.CM_90_1044.model.ResponseModel;

public interface CM901044Service {
    /**
     * 공지사항 전체 리스트 조회
     * 
     * @return 공지사항 전체 리스트
     */
    ResponseEntity<ResponseModel<List<NoticeInfo>>> findByAllNotice();

    /**
     * 특정 공지사항 리스트 조회
     * 
     * @param searchKeyword 키워드
     * @param searchType    타입 (1:제목 + 내용, 2:제목, 3:내용)
     * @return 배너 리스트
     */
    ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindNotice(String searchKeyword, int searchType);

    /**
     * 특정 공지사항 리스트 조회
     * 
     * @param noticeId 공지사항 아이디
     * @return 공지사항 리스트
     */
    ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindIdNotice(int noticeId);

    /**
     * 공지사항 등록
     * 
     * @param entity 등록 할 공지사항 정보
     * @return 등록 여부
     */
    ResponseEntity<ResponseModel<Integer>> noticeUpload(NoticeInfo entity, String userId);

    /**
     * 공지사항 업데이트
     * 
     * @param entity 업데이트 할 공지사항 정보
     * @return 업데이트 여부
     */
    ResponseEntity<ResponseModel<Integer>> noticeUpdate(NoticeInfo entity, String userId);
}
