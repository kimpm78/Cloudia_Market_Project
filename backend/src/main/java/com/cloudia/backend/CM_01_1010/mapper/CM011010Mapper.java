package com.cloudia.backend.CM_01_1010.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_01_1001.model.User;

@Mapper
public interface CM011010Mapper {

    /**
     * ユーザーIDでユーザー情報を取得
     *
     * @param userId 取得対象のユーザーID
     * @return User オブジェクト
     */
    User findByUserId(Integer userId);

    /**
     * ユーザーを非アクティブ（退会）状態に変更し、退会理由を保存
     *
     * @param userId 非アクティブ化するユーザーID
     * @param reasonText 退会理由
     */
    void deactivateUser(@Param("userId") Integer userId, @Param("reasonText") String reasonText);

    /**
     * 会員の進行中の注文または予約商品の件数を取得
     * 退会可否を判定するために使用（0件である必要あり）
     *
     * @param memberNumber 会員番号
     * @return 進行中の注文件数
     */
    int countActiveOrders(@Param("memberNumber") String memberNumber);
}
