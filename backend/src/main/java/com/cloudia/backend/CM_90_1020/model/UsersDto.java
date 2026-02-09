package com.cloudia.backend.CM_90_1020.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UsersDto {
    private String memberNumber; // 사원 번호
    private Integer roleId; // 사용자 권한
    private String loginId; // 로그인 아이디
    private String password; // 비밀번호
    private String passwordConfirm; // 비밀번호 확인
    private String email; // 이메일 주소
    private String name; // 성명
    private Integer genderValue; // 성별
    private LocalDate birthDate; // 생년월일
    private String nationality; // 국적/지역
    private String phoneNumber; // 휴대폰 번호
    private Boolean termsAgreed; // 이용 약관
    private Integer userStatusValue; // 유저 상태
    private String note; // 비고
    private String postalCode;// 우편번호
    private String addressMain;// 주소
    private String addressDetail1;// 상세 주소 1
    private String addressDetail2;// 상세 주소 2
    private String addressDetail3;// 상세 주소 3
    private LocalDateTime createdAt; // 가입일
    private String updatedBy; // 업데이트자
    private LocalDateTime updatedAt; // 업데이트일
    private String refundAccountHolder;// 환불 예금주
    private String refundAccountNumber;// 환불 계좌번호
    private String refundAccountBank;// 환불 은행
    private String pccc;// 통관 번호
}
