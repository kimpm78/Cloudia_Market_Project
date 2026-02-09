package com.cloudia.backend.common.model.pg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 결제 실패(또는 사용자 닫힘/취소) 처리 요청 모델.
 * - PG에 별도 취소 요청을 보내지 않고 내부 상태만 실패로 기록할 때 사용한다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PGFailRequest {
    /** 주문 정보 */
    private Long orderId;
    private String orderNumber;

    /** PG 거래 정보 (선택) */
    private String tid;

    /** PG 구분 */
    private String pgType;

    /** 실패 사유(로그/DB 저장용) */
    private String reason;
}
