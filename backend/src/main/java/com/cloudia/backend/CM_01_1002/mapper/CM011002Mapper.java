package com.cloudia.backend.CM_01_1002.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.cloudia.backend.CM_01_1001.model.User;

@Mapper
public interface CM011002Mapper {

    /**
     * 指定したメールアドレスのユーザー数を取得（存在確認用）
     *
     * @param email 確認対象のメールアドレス
     * @return 該当メールアドレスを持つユーザー数
     */
    int countByEmail(String email);

    /**
     * 指定したメールアドレスでユーザー情報を取得
     *
     * @param email 確認対象のメールアドレス
     * @return Userオブジェクト
     */
    User findByEmail(String email);
}