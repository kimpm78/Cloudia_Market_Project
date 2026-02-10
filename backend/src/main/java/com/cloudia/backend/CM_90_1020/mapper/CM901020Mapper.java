package com.cloudia.backend.CM_90_1020.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1020.model.PasswordHistoryDto;
import com.cloudia.backend.CM_90_1020.model.UsersDto;

@Mapper
public interface CM901020Mapper {
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
    List<UsersDto> findByUsers(@Param("searchTerm") String searchTerm, @Param("searchType") int searchType);

    /**
     * 特定ユーザーを取得
     * 
     * @param searchTerm キーワード
     * @return ユーザー情報
     */
    UsersDto findByUser(@Param("searchTerm") String searchTerm);

    /**
     * ユーザー更新
     * 
     * @param entity ユーザー情報
     * @return 更新結果
     */
    int userUpload(UsersDto entity);

    /**
     * パスワード履歴を保存
     * 
     * @param entity ユーザー情報
     * @return 更新結果
     */
    int insertPasswordHistory(UsersDto entity);

    /**
     * 住所を更新
     * 
     * @param entity 住所情報
     * @return 更新結果
     */
    int addressUpload(UsersDto entity);

    /**
     * パスワード履歴を取得
     * 
     * @param memberNumber 社員番号
     * @return パスワード履歴一覧
     */
    List<PasswordHistoryDto> findRecentPasswordHistory(@Param("memberNumber") String memberNumber,
            @Param("sixMonthsAgo") LocalDateTime sixMonthsAgo);
}
