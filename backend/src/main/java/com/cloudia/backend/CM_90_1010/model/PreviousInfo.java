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
public class PreviousInfo {
    private Integer previousNetSales;       // 前日の純売上状況
    private Integer settlementAmount;       // 売上金額
    private Integer refundAmount;           // 返金金額
    private Integer orderCount;             // 注文件数
    private Integer refundCount;            // 返金／返品件数
    private Integer shipmentProcessedCount; // 出荷処理件数
    private Integer deliveredCount;         // 配送完了件数
}