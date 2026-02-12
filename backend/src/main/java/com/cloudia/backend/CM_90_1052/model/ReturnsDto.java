package com.cloudia.backend.CM_90_1052.model;

import java.time.LocalDateTime;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
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
public class ReturnsDto {
    private int returnId;                   // 返品ID
    private String orderNo;                 // 要求番号
    private String orderNumber;             // 購入番号
    private String customerId;              // 社員番号
    private String memberNumber;            // 会員番号
    private LocalDateTime completedAt;      // 交換/返金完了日
    private String reason;                  // メモ
    private int totalAmount;                // 合計返金額
    private int shippingFeeCustomerAmount;  // 配送料(顧客負担)
    private int shippingFeeSellerAmount;    // 配送料(店舗負担)
    private int refundAmount;               // 返金額
    private int returnStatusValue;          // 状態
    private String requestedAt;             // 返金/交換依頼日
    private String updatedBy;               // 更新者
    private LocalDateTime updatedAt;        // 更新日
}
