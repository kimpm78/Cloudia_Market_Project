package com.cloudia.backend.common.model.pg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PG ReturnUrl 요청 DTO
 * PG 결제 완료 후 our-server로 전달되는 데이터를 담는 모델
 * encData 기반 decrypt → approve 로 이어지는 핵심 구조
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PGReturnRequest {

    /** PG returnUrl 에서 넘어오는 암호화 데이터 */
    private String encData;

    /** PG returnUrl 에서 직접 넘어오는 주문번호 */
    private String orderNo;

    /** PG 종류 (COOKIEPAY, TOSS, INICIS 등) */
    private String pgType;

    /** PG 가 return 시 함께 넘길 수 있는 코드/메시지 (옵션) */
    private String resultCode;     // 직접 넘어올 수 있음
    private String resultMessage;  // 직접 넘어올 수 있음
}
