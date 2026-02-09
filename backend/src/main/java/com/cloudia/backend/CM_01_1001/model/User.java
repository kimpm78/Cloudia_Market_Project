package com.cloudia.backend.CM_01_1001.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class User implements UserDetails {

    private Integer userId;
    private String memberNumber;
    private Integer roleId; // 사용자 권한

    @NotBlank(message = "ID를 입력해야 합니다.")
    @Size(min = 6, message = "ID는 최소 6자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z0-9]+$", message = "아이디는 영문자와 숫자를 모두 포함해야 하며, 다른 문자는 사용할 수 없습니다.")
    private String loginId; // 로그인 아이디

    @NotBlank(message = "비밀번호를 입력해야 합니다")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$", message = "비밀번호는 영문자, 숫자, 특수문자를 모두 포함해야 합니다.")
    private String password; // 비밀번호

    @NotBlank(message = "비밀번호 확인을 입력해야 합니다.")
    private String passwordConfirm; // 비밀번호 확인

    @NotBlank(message = "이메일을 입력해야 합니다")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email; // 이메일 주소

    @NotNull(message = "성명을 입력해야 합니다.")
    private String name; // 성명

    private String genderType; // 성별 타입
    private Integer genderValue;

    @NotBlank(message = "생년월일은 필수 입력 값입니다.")
    private String birthDate; // 생년월일

    @NotBlank(message = "국가를 선택해야 합니다.")
    private String nationality; // 국적/지역

    @NotBlank(message = "휴대폰 번호를 입력해야 합니다")
    private String phoneNumber; // 휴대폰 번호

    private String postalCode; // 우편번호
    private String addressMain; // 주소
    private String addressDetail1; // 상세 주소1
    private String addressDetail2; // 상세 주소2
    private String addressDetail3; // 상세 주소3

    private String refundAccountBank; // 환불 계좌 은행명
    private String refundAccountNumber; // 환불 계좌 번호
    private String refundAccountHolder; // 환불 계좌 예금주

    @Pattern(regexp = "^$|^P\\d{12}$", message = "개인통관고유부호는 P로 시작하는 13자리여야 합니다.")
    private String pccc; // 개인통관고유부호
    private String reasonText; // 사유 (탈퇴/정지 등)

    @NotNull(message = "이용약관 및 개인정보처리방침에 동의해야 합니다.")
    private Boolean termsAgreed; // 이용 약관

    private String userStatusType; // 유저 상태 타입
    private Integer userStatusValue;
    private String note; // 비고
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Integer permissionLevel;

    // --- Spring Security 관련 ---
    private List<GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getUsername() {
        return this.loginId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
