package com.cloudia.backend.CM_01_1001.model;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private Integer addressId;       // 住所ID
    private String memberNumber;     // 会員番号
    private String addressNickname;  // 配送先名（別名）
    private String recipientName;    // 受取人名

    @NotBlank(message = "郵便番号を入力してください")
    private String postalCode;       // 郵便番号

    @NotBlank(message = "住所を入力してください")
    private String addressMain;      // 基本住所

    @NotBlank(message = "詳細住所1を入力してください")
    private String addressDetail1;   // 詳細住所1
    private String addressDetail2;   // 詳細住所2
    private String addressDetail3;   // 詳細住所3
    private String recipientPhone;   // 受取人連絡先
    private Boolean isDefault;       // デフォルト配送先かどうか
    private int isActive;            // 削除フラグ

    private String createdBy;        // 作成者
    private LocalDateTime createdAt; // 作成日時
    private String updatedBy;        // 更新者
    private LocalDateTime updatedAt; // 更新日時
    private LocalDateTime deletedAt; // 削除日時
}