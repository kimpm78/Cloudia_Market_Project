package com.cloudia.backend.CM_90_1044.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1044.model.NoticeInfo;

@Mapper
public interface CM901044Mapper {
    /**
     * 공지사항 전체 리스트 조회
     * 
     * @return 공지사항 전체 리스트
     */
    List<NoticeInfo> findByAllNotice();

    /**
     * 특정 공지사항 리스트 조회
     * 
     * @param searchKeyword 키워드
     * @param searchType    타입 (1:제목 + 내용, 2:제목, 3:내용)
     * @return 배너 리스트
     */
    List<NoticeInfo> findByNotice(@Param("searchKeyword") String searchKeyword, @Param("searchType") int searchType);

    /**
     * 특정 공지사항 리스트 조회
     * 
     * @param noticeId 공지사항 아이디
     * @return 공지사항 리스트
     */
    List<NoticeInfo> findIdNotice(@Param("noticeId") int noticeId);

    /**
     * 공지사항 등록
     * 
     * @param entity 등록 할 공지사항 정보
     * @return 등록 여부
     */
    int noticeUpload(NoticeInfo entity);

    /**
     * 공지사항 업데이트
     * 
     * @param entity 업데이트 할 공지사항 정보
     * @return 업데이트 여부
     */
    int noticeUpdate(NoticeInfo entity);
}
