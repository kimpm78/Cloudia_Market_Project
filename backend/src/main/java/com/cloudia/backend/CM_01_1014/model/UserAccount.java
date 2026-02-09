package com.cloudia.backend.CM_01_1014.model;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {
    private String refundAccountBank; // 은행명
    private String refundAccountNumber; // 계좌번호
    private String refundAccountHolder; // 예금주
}