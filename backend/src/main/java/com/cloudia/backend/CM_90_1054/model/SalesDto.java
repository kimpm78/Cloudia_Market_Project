package com.cloudia.backend.CM_90_1054.model;

import java.math.BigDecimal;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
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
public class SalesDto {
    private String month; // 합계 달
    private BigDecimal totalAmount; // 합계 총액
}
