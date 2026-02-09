package com.cloudia.backend.auth.mapper;

import com.cloudia.backend.auth.model.PasswordHistory;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PasswordHistoryMapper {
    /**
     * 新しいパスワード履歴を保存します。
     *
     * @param passwordHistory パスワード履歴情報
     */
    void insertPasswordHistory(PasswordHistory passwordHistory);

    /**
     * 会員番号で過去のパスワード履歴を取得します。
     *
     * @param memberNumber 取得対象の会員番号
     * @return パスワード履歴リスト
     */
    List<PasswordHistory> findByMemberNumber(String memberNumber);
}