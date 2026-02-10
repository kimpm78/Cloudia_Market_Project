package com.cloudia.backend.CM_06_1001.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注文／決済処理で使用する配送先情報
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long addressId;         // 住所ID（PK）
    private String addressNickname; // 住所の別名（例：自宅、職場など）
    private String recipientName;   // 受取人氏名
    private String recipientPhone;  // 受取人連絡先
    private String postalCode;      // 郵便番号
    private String addressMain;     // 基本住所
    private String addressDetail1;  // 詳細住所1
    private String addressDetail2;  // 詳細住所2
    private String addressDetail3;  // 詳細住所3
    private String memo;            // 配送メモ（必要に応じて使用）
    private Boolean isDefault;      // デフォルト配送先かどうか
}