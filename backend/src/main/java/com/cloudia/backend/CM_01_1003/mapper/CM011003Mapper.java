package com.cloudia.backend.CM_01_1003.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.cloudia.backend.CM_01_1001.model.User;

@Mapper
public interface CM011003Mapper {

    /**
     * 주어진 이메일을 가진 사용자의 수를 조회
     * 
     * @param email 확인할 이메일 주소
     * @return 해당 이메일을 가진 사용자의 수 (0 또는 1)
     */
    int countByEmail(String email);

    /**
     * 주어진 이메일로 사용자 정보를 조회
     * 
     * @param email 조회할 사용자의 이메일 주소
     * @return User 객체
     */
    User findByEmail(String email);

    /**
     * 사용자의 비밀번호를 업데이트
     * 
     * @param email       비밀번호를 변경할 사용자의 이메일
     * @param newPassword 암호화된 새 비밀번호
     * @return 업데이트된 행의 수
     */
    int updatePassword(User user);
}