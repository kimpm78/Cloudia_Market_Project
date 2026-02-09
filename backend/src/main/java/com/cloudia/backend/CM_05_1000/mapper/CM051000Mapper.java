package com.cloudia.backend.CM_05_1000.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_05_1000.model.NoticeInfo;

@Mapper
public interface CM051000Mapper {

    /**
     * 공지사항 전체 조회
     *
     * @return 전체 공지사항 목록
     */
    List<NoticeInfo> findByAllNotice();

    /**
     * 공지사항 검색
     *
     * @param searchKeyword 검색 키워드
     * @param searchType    검색 유형 (1: 제목+내용, 2: 제목, 3: 내용)
     * @return 검색된 공지사항 목록
     */
    List<NoticeInfo> findByNotice(@Param("searchKeyword") String searchKeyword,
    @Param("searchType") int searchType);

    /**
     * 공지사항 상세 조회
     *
     * @param noticeId 공지사항 아이디
     * @return 해당 공지사항 정보
     */
    List<NoticeInfo> findIdNotice(@Param("noticeId") int noticeId);

    /**
     * 공지사항 단건 조회
     *
     * @param noticeId 공지사항 ID
     * @return 해당 공지사항 정보
     */
    NoticeInfo findIdNoticeOne(@Param("noticeId") int noticeId);

    /**
     * 이전 공지사항 조회 (현재 ID보다 작은 ID 중 가장 큰 값)
     *
     * @param noticeId 기준 공지사항 ID
     * @return 이전 공지사항
     */
    NoticeInfo findPrevNotice(@Param("noticeId") int noticeId);

    /**
     * 다음 공지사항 조회 (현재 ID보다 큰 ID 중 가장 작은 값)
     *
     * @param noticeId 기준 공지사항 ID
     * @return 다음 공지사항
     */
    NoticeInfo findNextNotice(@Param("noticeId") int noticeId);

    /**
     * 공지사항 조회수 증가
     *
     * @param noticeId 공지사항 ID
     * @return 업데이트된 행 수
     */
    int incrementViewCount(@Param("noticeId") int noticeId);

    /**
     * 공지사항 등록
     *
     * @param entity 등록할 공지사항 정보
     * @return 등록 성공 시 1, 실패 시 0
     */
    int noticeUpload(NoticeInfo entity);

    /**
     * 공지사항 수정
     *
     * @param entity 수정할 공지사항 정보
     * @return 수정 성공 시 1, 실패 시 0
     */
    int noticeUpdate(NoticeInfo entity);
    /**
     * 공지사항 삭제
     *
     * @param noticeId 삭제할 공지사항 ID
     * @return 삭제 성공 시 1, 실패 시 0
     */
    int deleteNotice(@Param("noticeId") Long noticeId);
}
