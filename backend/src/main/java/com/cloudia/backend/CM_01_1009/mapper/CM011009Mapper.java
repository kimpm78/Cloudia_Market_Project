package com.cloudia.backend.CM_01_1009.mapper;

import com.cloudia.backend.CM_01_1001.model.User;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CM011009Mapper {
    /**
     * ログインIDでユーザー情報を取得
     *
     * @param loginId 取得対象ユーザーのログインID
     * @return Userオブジェクト
     */
    User findByLoginId(String loginId);

    /**
     * ユーザーのパスワードを更新
     *
     * @param userId 更新対象ユーザーのID
     * @param newPassword 暗号化された新しいパスワード
     */
    void updatePassword(@Param("userId") Integer userId, @Param("newPassword") String newPassword);

    /**
     * パスワード履歴を取得
     */
    List<String> findRecentPasswords(@Param("memberNumber") String memberNumber,
            @Param("sixMonthsAgo") LocalDateTime limitDate);

    /**
     * 変更したパスワードを履歴テーブルに保存
     */
    void insertPasswordHistory(@Param("memberNumber") String memberNumber, @Param("password") String password);
}