package com.cloudia.backend.common.model.pg;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * PG 결제 승인 요청 공통 모델
 * - 주문 기준 정보 + PG 구분 + PG 거래 ID + 금액 정도만 가진다.
 * - PG의 상세 응답값(결과코드/메시지)은 PGResult 에서 관리.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PGApproveRequest {

    /** 주문 정보 */
    private Long orderId;        // 주문 ID (내부 PK, 선택)
    private String orderNumber;  // 주문 번호 (가맹점 주문번호)
    /** PG 구분 */
    private String pgType;       // PG 종류: "COOKIEPAY", "TOSS", "INICIS" 등
    /** PG 거래 정보 */
    private String tid;          // PG 거래 번호 (TID, payments.transaction_id로 저장)
    private Integer amount;      // 승인 금액 (검증용)
}
