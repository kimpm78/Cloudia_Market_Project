package com.cloudia.backend.CM_90_1031.model;

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
public class ageDto {
    private String ageGroup;         // 나이 그룹
    private Integer userCount;       // 그룹별 카운트
    private Double avgAmountPerUser; // 그룹별 평균 단가
}
