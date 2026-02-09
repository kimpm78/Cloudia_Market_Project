package com.cloudia.backend.CM_90_1020.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1020.model.PasswordHistoryDto;
import com.cloudia.backend.CM_90_1020.model.UsersDto;

@Mapper
public interface CM901020Mapper {
    /**
     * 유저 전체 리스트 조회
     * 
     * @return 유저 전체 리스트
     */
    List<UsersDto> findByAllUsers();

    /**
     * 유저 조회
     * 
     * @param searchTerm 키워드
     * @param searchType 타입 (1:사원 번호, 2:ID)
     * @return 유저 리스트
     */
    List<UsersDto> findByUsers(@Param("searchTerm") String searchTerm, @Param("searchType") int searchType);

    /**
     * 특정 유저 조회
     * 
     * @param searchTerm 키워드
     * @return 유저 리스트
     */
    UsersDto findByUser(@Param("searchTerm") String searchTerm);

    /**
     * 유저 업데이트
     * 
     * @param entity 유저 정보
     * @return 업데이트 결과
     */
    int userUpload(UsersDto entity);

    /**
     * 비밀번호 히스토리 저장
     * 
     * @param entity 유저 정보
     * @return 업데이트 결과
     */
    int insertPasswordHistory(UsersDto entity);

    /**
     * 주소 업데이트
     * 
     * @param entity 주소 정보
     * @return 업데이트 결과
     */
    int addressUpload(UsersDto entity);

    /**
     * 비밀번호 히스토리 조회
     * 
     * @param memberNumber 사원 번호
     * @return 비밀번호 히스토리 리스트
     */
    List<PasswordHistoryDto> findRecentPasswordHistory(@Param("memberNumber") String memberNumber,
            @Param("sixMonthsAgo") LocalDateTime sixMonthsAgo);
}
