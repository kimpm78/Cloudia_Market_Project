package com.cloudia.backend.CM_90_1052.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

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
public class RefundSearchRequestDto {
    private String requestNo;               // 依頼番号
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFrom;             // 返金日付（From）
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateTo;               // 返金日付（To）
    private Integer orderStatusValue;       // 注文ステータス
    private String paymentMethod;           // 支払方法
}
