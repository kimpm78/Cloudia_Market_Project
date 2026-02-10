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
public class WeeklySales {
    private Integer dayNum;      // 曜日（数値）
    private String dayOfWeek;    // 曜日（文字列）
    private Integer totalAmount; // 合計金額
}
