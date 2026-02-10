package com.cloudia.backend.CM_01_1003.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.cloudia.backend.CM_01_1001.model.User;

@Mapper
public interface CM011003Mapper {

    /**
     * 指定されたメールアドレスを持つユーザー数を取得
     * 
     * @return 該当メールアドレスを持つユーザー数（0 または 1）
     */
    int countByEmail(String email);

    /**
     * 指定されたメールアドレスでユーザー情報を取得
     * 
     * @return User オブジェクト
     */
    User findByEmail(String email);

    /**
     * ユーザーのパスワードを更新
     * 
     * @param newPassword 暗号化された新しいパスワード
     * @return 更新された行数
     */
    int updatePassword(User user);
}