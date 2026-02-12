package com.cloudia.backend.CM_01_1007.mapper;

import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_01_1007.model.UserProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CM011007Mapper {

    /**
     * ログインIDを使用して users テーブルからプロフィール情報を取得
     * 
     * @param loginId 取得対象ユーザーのログインID
     * @return プロフィールオブジェクト
     */
    UserProfile findProfileByLoginId(String loginId);

    /**
     * ログインIDを使用して users テーブルから User 全体オブジェクトを取得
     * 
     * @param loginId 取得対象ユーザーのログインID
     * @return Userオブジェクト
     */
    User findUserByLoginId(String loginId);

    /**
     * users テーブルのプロフィール関連情報を更新
     * 
     * @param user Userオブジェクト
     */

    void updateUserProfile(User user);
}