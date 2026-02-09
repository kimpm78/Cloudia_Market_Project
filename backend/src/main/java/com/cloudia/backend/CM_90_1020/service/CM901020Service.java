package com.cloudia.backend.CM_90_1020.service;

import java.util.List;

import com.cloudia.backend.CM_90_1020.model.UsersDto;

public interface CM901020Service {
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
    List<UsersDto> getFindUsers(String searchTerm, int searchType);

    /**
     * 특정 유저 조회
     * 
     * @param searchTerm 키워드
     * @return 유저 리스트
     */
    UsersDto getFindUser(String searchTerm);

    /**
     * 유저 업데이트
     * 
     * @param userInfo 유저 정보
     * @return 성공 여부
     */
    Integer postUserUpdate(UsersDto entity, String userId);
}
