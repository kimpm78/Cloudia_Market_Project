package com.cloudia.backend.CM_90_1031.constants;

public class CM901031MessageConstant {
    private CM901031MessageConstant() {
    }

    // 연령대 조회 관련
    public static final String USER_FIND_ALL_START = "유저 전체 리스트 조회 시작";
    public static final String USER_FIND_ALL_COMPLETE = "유저 전체 리스트 조회 완료, 조회된 유저 수: {}";
    public static final String SUCCESS_USER_FIND = "유저 조회 성공";
    public static final String USER_FIND_ALL_DB_ERROR = "유저 전체 리스트 조회 중 DB 오류 발생: {}";
    public static final String USER_FIND_ALL_NULL_ERROR = "유저 전체 리스트 조회 중 NullPointerException 발생: {}";
    public static final String USER_FIND_ALL_UNEXPECTED_ERROR = "유저 전체 리스트 조회 중 예상치 못한 오류 발생: {}";
}
