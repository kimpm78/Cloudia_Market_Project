package com.cloudia.backend.CM_01_1014.mapper;

import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_01_1014.model.UserAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CM011014Mapper {
    // 계좌 정보 조회
    UserAccount findAccountByLoginId(String loginId);

    // 업데이트를 위한 유저 조회
    User findUserByLoginId(String loginId);

    // 계좌 정보 업데이트
    void updateUserAccount(User user);
}