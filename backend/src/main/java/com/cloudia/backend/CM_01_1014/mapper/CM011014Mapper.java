package com.cloudia.backend.CM_01_1014.mapper;

import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_01_1014.model.UserAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CM011014Mapper {
    // 口座情報取得
    UserAccount findAccountByLoginId(String loginId);

    // 更新のためのユーザー取得
    User findUserByLoginId(String loginId);

    // 口座情報更新
    void updateUserAccount(User user);
}