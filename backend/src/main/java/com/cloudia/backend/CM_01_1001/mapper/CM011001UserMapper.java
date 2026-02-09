package com.cloudia.backend.CM_01_1001.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.cloudia.backend.CM_01_1001.model.User;

import io.lettuce.core.dynamic.annotation.Param;

@Mapper
public interface CM011001UserMapper {

    /**
     * 새로운 사용자 정보 저장 (회원가입)
     * 
     * @param user 저장할 사용자 정보 객체
     */
    int insertUser(User user);

    /**
     * 주어진 로그인 ID의 사용자를 조회
     * 
     * @param loginId 확인할 로그인 ID
     * @return 해당 ID를 가진 사용자의 수
     */
    int countByLoginId(String loginId);

    /**
     * 주어진 로그인 ID로 사용자 정보를 조회
     * * @param loginId 확인할 로그인 ID
     * 
     * @return User 객체
     */
    User findByLoginId(String loginId);

    /**
     * 주어진 이메일 ID의 사용자 수를 조회하여 중복 여부를 확인
     * 
     * @param email 확인할 이메일 ID
     * @return 해당 이메일 ID를 가진 사용자 유무
     */
    int countByEmail(String email);

    /**
     * 모든 사용자 목록을 조회
     * 
     * @return 사용자 정보 리스트
     */
    List<User> findAllUsers();

    User findByUserId(Integer userId);

    /**
     * 회원번호로 사용자 정보를 조회
     *
     * @param memberNumber 회원번호
     * @return User 정보 (없으면 null)
     */
    User findByMemberNumber(String memberNumber);

    /**
     * 다음 회원 번호(member_number)를 조회
     * * @return 8자리 회원 번호 문자열
     */
    String getNextMemberNumber();

    /**
     * 개인통관고유부호(PCCC) 중복 횟수 조회
     * 
     * @param pccc 확인할 통관부호
     * @return 중복된 데이터 개수
     */
    int countByPccc(String pccc);

    /**
     * 특정 권한 ID를 가진 사용자들의 이메일 주소 목록 조회
     *
     * @param roleId 권한 ID
     * @return 이메일 주소 리스트
     */
    List<String> findEmailsByRoleId(@Param("roleId") int roleId);
}
