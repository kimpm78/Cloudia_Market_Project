package com.cloudia.backend.CM_06_1001.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfo {
   private String paymentId;           // PK（UUIDなど、最大50文字）
   private Long orderId;               // 注文PK（orders.order_id）
   private String orderNumber;         // 注文番号（orders.order_number）
   private String pgType;              // PG種別
   private String paymentType;         // 決済方法コードタイプ（例："011"）
   private String paymentMethod;       // 決済手段コード値（"1": 銀行振込、"2": クレジットカード）
   private String pgProvider;          // PG Provider (예: "COOKIEPAY", "PAYVERSE")
   private String paymentStatusType;   // 決済ステータスコードタイプ（例："013"）
   private String paymentStatusCode;   // 決済ステータスコード値（1: 準備、2: 承認、3: 失敗）
   private Integer amount;             // 決済リクエスト金額（NOT NULL, >= 0）
   private String transactionId;       // PG取引番号（TID）または仮ID（NOT NULL）
   private String resultCode;          // PGレスポンスコード（例："0000" 成功）
   private String resultMsg;           // PGレスポンスメッセージ
   private String authCode;            // 承認番号
   private LocalDateTime approvedAt;   // 承認日時
   private LocalDateTime createdAt;
   private LocalDateTime updatedAt;
   private String createdBy;
   private String updatedBy;
}