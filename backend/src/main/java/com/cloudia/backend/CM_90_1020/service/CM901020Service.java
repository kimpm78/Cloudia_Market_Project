package com.cloudia.backend.CM_90_1020.service;

import java.util.List;

import com.cloudia.backend.CM_90_1020.model.UsersDto;

public interface CM901020Service {
    /**
     * ユーザー全件一覧を取得
     * 
     * @return ユーザー全件一覧
     */
    List<UsersDto> findByAllUsers();

    /**
     * ユーザー検索
     * 
     * @param searchTerm キーワード
     * @param searchType タイプ（1:社員番号、2:ID）
     * @return ユーザー一覧
     */
    List<UsersDto> getFindUsers(String searchTerm, int searchType);

    /**
     * 特定ユーザーを取得
     * 
     * @param searchTerm キーワード
     * @return ユーザー情報
     */
    UsersDto getFindUser(String searchTerm);

    /**
     * ユーザー更新
     * 
     * @param entity ユーザー情報
     * @return 更新結果
     */
    Integer postUserUpdate(UsersDto entity, String userId);
}