package com.cloudia.backend.CM_01_1001.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_01_1001.model.User;

@Mapper
public interface CM011001UserMapper {
    /**
     * 新しいユーザー情報を保存（会員登録）
     *
     * @param user 保存するユーザー情報オブジェクト
     */
    int insertUser(User user);

    /**
     * 指定されたログインIDのユーザー数を取得
     *
     * @param loginId 確認するログインID
     * @return 該当するログインIDを持つユーザー数
     */
    int countByLoginId(String loginId);

    /**
     * 指定されたログインIDでユーザー情報を取得
     *
     * @param loginId 確認するログインID
     * @return User オブジェクト
     */
    User findByLoginId(String loginId);

    /**
     * 指定されたメールアドレスのユーザー数を取得し、重複有無を確認
     *
     * @param email 確認するメールアドレス
     * @return 該当するメールアドレスを持つユーザー数
     */
    int countByEmail(String email);

    /**
     * すべてのユーザー一覧を取得
     *
     * @return ユーザー情報リスト
     */
    List<User> findAllUsers();

    User findByUserId(Integer userId);

    /**
     * 会員番号でユーザー情報を取得
     *
     * @param memberNumber 会員番号
     * @return User 情報（存在しない場合は null）
     */
    User findByMemberNumber(String memberNumber);

    /**
     * 次の会員番号（member_number）を取得
     *
     * @return 8桁の会員番号文字列
     */
    String getNextMemberNumber();

    /**
     * 個人通関固有符号（PCCC）の重複件数を取得
     *
     * @param pccc 確認する通関符号
     * @return 重複データ件数
     */
    int countByPccc(String pccc);

    /**
     * 指定した権限IDを持つユーザーのメールアドレス一覧を取得
     *
     * @param roleId 権限ID
     * @return メールアドレス一覧
     */
    List<String> findEmailsByRoleId(@Param("roleId") int roleId);
}
