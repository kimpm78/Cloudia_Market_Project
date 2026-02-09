package com.cloudia.backend.CM_01_1001.model;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private Integer addressId; // 주소 아이디

    private String memberNumber; // 회원번호
    private String addressNickname; // 배송지명 (별칭)
    private String recipientName; // 수령인 이름

    @NotBlank(message = "우편번호를 입력해야 합니다")
    private String postalCode; // 우편번호

    @NotBlank(message = "주소를 입력해야 합니다")
    private String addressMain; // 기본 주소

    @NotBlank(message = "상세 주소1을 입력해야 합니다")
    private String addressDetail1; // 상세 주소1

    private String addressDetail2; // 상세 주소2
    private String addressDetail3; // 상세 주소3
    private String recipientPhone; // 수령인 연락처
    private Boolean isDefault; // 기본 배송지 여부
    private int isActive; // 삭제 여부

    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

}