package com.cloudia.backend.CM_90_1051.model;

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
public class AddressDto {
    private Integer addressId;      // 住所ID
    private String memberNumber;    // 会員番号
    private String addressNickname; // 配送先名（別名）
    private String recipientName;   // 受取人氏名
    private String postalCode;      // 郵便番号
    private String addressMain;     // 住所（都道府県・市区町村など）
    private String addressDetail1;  // 住所詳細1
    private String addressDetail2;  // 住所詳細2
    private String addressDetail3;  // 住所詳細3
    private String recipientPhone;  // 受取人連絡先
    private Boolean isDefault;      // デフォルト配送先フラグ
    private int isActive;           // 削除フラグ
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
