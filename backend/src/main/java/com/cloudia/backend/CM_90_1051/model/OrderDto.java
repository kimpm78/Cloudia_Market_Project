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
public class OrderDto {
    private Integer orderId;            // 注文ID
    private String memberNumber;        // 会員番号
    private String loginId;             // ログインID
    private String email;               // メールアドレス
    private String name;                // 氏名
    private String orderNumber;         // 注文番号
    private Double totalAmount;         // 注文合計金額
    private Integer paymentValue;       // 支払方法
    private Integer orderStatusValue;   // ステータス
    private String shippingCompany;     // 配送業者
    private String trackingNumber;      // 追跡番号
    private String shippingDate;        // 実際の発送日
    private String deliveryDate;        // 配送予定日
    private LocalDateTime orderDate;    // 注文日時
    private String updatedBy;           // 更新者
    private LocalDateTime updatedAt;    // 更新日時
}
