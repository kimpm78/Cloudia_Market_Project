package com.cloudia.backend.CM_01_1009.mapper;

import com.cloudia.backend.CM_01_1001.model.User;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CM011009Mapper {
    /**
     * 로그인 ID로 사용자 정보를 조회
     * 
     * @param loginId 조회할 사용자의 로그인 ID
     * @return User 객체
     */
    User findByLoginId(String loginId);

    /**
     * 사용자의 비밀번호를 업데이트
     * 
     * @param userId      업데이트할 사용자의 ID
     * @param newPassword 암호화된 새로운 비밀번호
     */
    void updatePassword(@Param("userId") Integer userId, @Param("newPassword") String newPassword);

    /**
     * 비밀번호 히스토리 조회
     */
    List<String> findRecentPasswords(@Param("memberNumber") String memberNumber,
            @Param("sixMonthsAgo") LocalDateTime limitDate);

    /**
     * 변경된 비밀번호를 히스토리 테이블에 저장
     */
    void insertPasswordHistory(@Param("memberNumber") String memberNumber, @Param("password") String password);
}