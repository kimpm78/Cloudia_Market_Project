package com.cloudia.backend.CM_90_1020.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class UsersDto {
    private String memberNumber;        // 社員番号
    private Integer roleId;             // ユーザー権限
    private String loginId;             // ログインID
    private String password;            // パスワード
    private String passwordConfirm;     // パスワード確認
    private String email;               // メールアドレス
    private String name;                // 氏名
    private Integer genderValue;        // 性別
    private LocalDate birthDate;        // 生年月日
    private String nationality;         // 国籍／地域
    private String phoneNumber;         // 携帯電話番号
    private Boolean termsAgreed;        // 利用規約
    private Integer userStatusValue;    // ユーザーステータス
    private String note;                // 備考
    private String postalCode;          // 郵便番号
    private String addressMain;         // 住所
    private String addressDetail1;      // 住所詳細1
    private String addressDetail2;      // 住所詳細2
    private String addressDetail3;      // 住所詳細3
    private LocalDateTime createdAt;    // 登録日
    private String updatedBy;           // 更新者
    private LocalDateTime updatedAt;    // 更新日
    private String refundAccountHolder; // 返金口座名義
    private String refundAccountNumber; // 返金口座番号
    private String refundAccountBank;   // 返金銀行
    private String pccc;                // 個人通関固有番号 (PCCC-KRのみ)
}