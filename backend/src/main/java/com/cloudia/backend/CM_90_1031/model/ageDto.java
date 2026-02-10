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
    private String ageGroup;         // 年齢グループ
    private Integer userCount;       // グループ別件数
    private Double avgAmountPerUser; // グループ別平均単価
}
