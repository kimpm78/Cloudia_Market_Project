package com.cloudia.backend.CM_90_1051.model;

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
    private String dateFrom; // 시작일
    private String dateTo; // 종료일
    private int paymentMethod;// 결제수단
    private String memberNumber;// 사원ID
    private String orderNumber;// 주문ID
    private int orderStatusValue;// 상태
    private String carrier; // 배송 회사
    private String trackingNumber; // 배송 조회 번호
}
