package com.cloudia.backend.CM_01_1005.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCancelRequest {
    private String orderNo;
    private String reason;

    // 계좌이체 환불용 필드
    private String bankName;
    private String accountNumber;
    private String accountHolder;
}