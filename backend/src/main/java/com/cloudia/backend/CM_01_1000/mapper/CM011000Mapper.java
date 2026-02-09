package com.cloudia.backend.CM_01_1000.mapper;

import com.cloudia.backend.CM_01_1001.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CM011000Mapper {
    /**
     * ログインIDでユーザー情報と権限をあわせて取得します。
     *
     * @param loginId ユーザーのログインID
     * @return 権限情報を含むUserオブジェクト
     */
    User findByLoginId(String loginId);
}