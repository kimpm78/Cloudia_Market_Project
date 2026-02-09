package com.cloudia.backend.CM_90_1010.model;

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
public class WeeklySalesDto {
    private Integer dayNum;      // 요일(숫자)
    private String dayOfWeek;    // 요일(문자)
    private Integer totalAmount; // 총금액
}
