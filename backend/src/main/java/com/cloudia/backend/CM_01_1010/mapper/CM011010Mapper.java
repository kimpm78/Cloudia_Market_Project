package com.cloudia.backend.CM_01_1010.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_01_1001.model.User;

@Mapper
public interface CM011010Mapper {

    /**
     * 사용자 ID로 사용자 정보를 조회
     *
     * @param userId 조회할 사용자의 ID
     * @return User 객체
     */
    User findByUserId(Integer userId);

    /**
     * 사용자를 비활성(탈퇴) 상태로 변경하고 탈퇴 사유를 저장
     *
     * @param userId     비활성화할 사용자의 ID
     * @param reasonText 탈퇴 사유
     */
    void deactivateUser(@Param("userId") Integer userId, @Param("reasonText") String reasonText);

    /**
     * 회원의 진행 중인 주문 또는 예약 상품 건수를 조회
     * 탈퇴 가능 여부를 판단하기 위해 사용됨 (0건이어야 탈퇴 가능)
     *
     * @param memberNumber 회원 번호
     * @return 진행 중인 주문 건수
     */
    int countActiveOrders(@Param("memberNumber") String memberNumber);
}
