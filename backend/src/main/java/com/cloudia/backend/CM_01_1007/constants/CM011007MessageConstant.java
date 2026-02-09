package com.cloudia.backend.CM_01_1007.constants;

public final class CM011007MessageConstant {

    private CM011007MessageConstant() {
    }

    public static final String CONTROLLER_GET_PROFILE_START = "프로필 정보 조회 요청 시작: loginId={}";
    public static final String CONTROLLER_GET_PROFILE_END = "프로필 정보 조회 요청 완료: loginId={}";
    public static final String CONTROLLER_UPDATE_PROFILE_START = "프로필 정보 업데이트 요청 시작: loginId={}";
    public static final String CONTROLLER_UPDATE_PROFILE_END = "프로필 정보 업데이트 요청 완료: loginId={}";
    public static final String SERVICE_GET_PROFILE = "서비스: 프로필 정보 조회, loginId={}";
    public static final String SERVICE_UPDATE_PROFILE = "서비스: 프로필 정보 업데이트, loginId={}";
    public static final String SUCCESS_UPDATE_PROFILE = "프로필이 성공적으로 업데이트되었습니다.";
    public static final String FAIL_USER_NOT_FOUND = "사용자를 찾을 수 없습니다.";
}