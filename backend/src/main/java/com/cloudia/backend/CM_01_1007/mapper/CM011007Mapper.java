package com.cloudia.backend.CM_01_1007.mapper;

import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_01_1007.model.UserProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CM011007Mapper {

    /**
     * 로그인 ID를 사용하여 users 테이블에서 프로필 정보를 조회
     * 
     * @param loginId 조회할 사용자의 로그인 ID
     * @return 프로필 객체
     */
    UserProfile findProfileByLoginId(String loginId);

    /**
     * 로그인 ID를 사용하여 users 테이블에서 전체 User 객체를 조회
     * 
     * @param loginId 조회할 사용자의 로그인 ID
     * @return User 객체
     */
    User findUserByLoginId(String loginId);

    /**
     * users 테이블의 프로필 관련 정보를 업데이트
     * 
     * @param user User 객체
     */

    void updateUserProfile(User user);
}