package com.cloudia.backend.CM_90_1050.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SearchRequestDto {
    private String dateFrom; // 開始日
    private String dateTo;   // 終了日
}
