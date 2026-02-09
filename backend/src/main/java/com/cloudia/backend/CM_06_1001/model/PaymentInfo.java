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
   /**
    * 기본 키 / 참조키 (PK / FK)
    */
   private String paymentId;     // PK (UUID 등, 최대 50자)
   private Long orderId;         // 주문 PK (orders.order_id)
   private String orderNumber;   // 주문 번호 (orders.order_number)

   /**
    * PG 정보 및 결제 상태
    */
   private String pgType;            // PG 종류 (COOKIEPAY, PAYVERSE 등) - DB 컬럼 없음(로직용)
   private String paymentType;       // 결제방법 코드타입 (예: "011")
   private String paymentMethod;     // 결제수단 코드값 ("1": 계좌이체, "2": 신용카드)
   private String pgProvider;        // PG Provider (예: "COOKIEPAY", "PAYVERSE")
   private String paymentStatusType; // 결제 상태 코드타입 (예: "013")
   private String paymentStatusCode; // 결제 상태 코드값 (1: 준비, 2: 승인, 3: 실패)

   /**
    * 결제 금액 / 승인 결과
    */
   private Integer amount;        // 결제 요청 금액 (NOT NULL, >= 0)
   private String transactionId;  // PG 거래번호(TID) 또는 임시 ID (NOT NULL)
   private String resultCode;     // PG 응답코드 (예: "0000" 성공)
   private String resultMsg;      // PG 응답 메시지
   private String authCode;       // 승인번호
   private LocalDateTime approvedAt; // 승인 시간

   /**
    * 감사(Audit)
    */
   private LocalDateTime createdAt;
   private LocalDateTime updatedAt;
   private String createdBy;
   private String updatedBy;
}
