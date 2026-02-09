package com.cloudia.backend.CM_05_1000.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_05_1000.model.NoticeInfo;
import com.cloudia.backend.CM_05_1000.model.ResponseModel;

public interface CM051000Service {
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
     * 특정 공지사항 및 이전/다음 공지사항 조회
     * 
     * @param noticeId 공지사항 아이디
     * @return Map 구조로 current, prev, next 공지사항 객체 반환
     */
    ResponseEntity<ResponseModel<Map<String, NoticeInfo>>> getFindIdNotice(int noticeId);

    /**
     * 공지사항 조회수 증가 (하루 1회 제한)
     *
     * @param noticeId 공지사항 ID
     * @param viewerKey 뷰어 키
     * @return 증가 여부
     */
    boolean increaseViewOncePerDay(int noticeId, String viewerKey);

    /**
     * 공지사항 등록
     * 
     * @param entity 등록 할 공지사항 정보
     * @return 등록 여부
     */
    ResponseEntity<ResponseModel<Integer>> noticeUpload(NoticeInfo entity);

    /**
     * 공지사항 업데이트
     * 
     * @param entity 업데이트 할 공지사항 정보
     * @return 업데이트 여부
     */
    ResponseEntity<ResponseModel<Integer>> noticeUpdate(NoticeInfo entity);

    /**
     * 공지사항 삭제
     * 
     * @param noticeId 삭제할 공지사항 ID
     * @return 삭제 처리 결과
     */
    ResponseEntity<ResponseModel<Integer>> deleteNotice(Long noticeId);
}
