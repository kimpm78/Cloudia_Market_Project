package com.cloudia.backend.common.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailDto {
    private String orderDate;         // 注文日
    private String dueDate;           // 入金期限
    private String orderNumber;       // 注文番号
    private String paymentMethod;     // 支払い方法
    private String paymentAmount;     // 支払い金額
    private String orderItems;        // 購入商品
    private String shippingDate;      // 発送予定日
    private String trackingNumber;    // 追跡番号（配送追跡番号）
    private String sendEmail;         // 宛先（単一）
    private List<String> sendEmails;  // 宛先（複数）
    private String pendingCount;      // 期限間近件数
    private String name;              // お客様名
    private String loginId;           // ログインID
    private String verificationCode;  // メール認証コード
}