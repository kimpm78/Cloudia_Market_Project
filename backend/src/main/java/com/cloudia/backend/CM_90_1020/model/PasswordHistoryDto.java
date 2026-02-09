package com.cloudia.backend.CM_90_1020.model;

import java.time.OffsetDateTime;
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
public class PasswordHistoryDto {
    private Integer userId; // 기본키
    private String memberNumber; // 사원 번호
    private String password; // 비밀번호
    private OffsetDateTime createdAt; // 생성일
}
