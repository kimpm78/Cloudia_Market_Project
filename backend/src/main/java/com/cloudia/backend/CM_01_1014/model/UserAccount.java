package com.cloudia.backend.CM_01_1014.model;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {
    private String refundAccountBank;   // 銀行名
    private String refundAccountNumber; // 口座番号
    private String refundAccountHolder; // 口座名義
}