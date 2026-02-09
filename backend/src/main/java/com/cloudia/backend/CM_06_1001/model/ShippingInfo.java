package com.cloudia.backend.CM_06_1001.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 주문/결제 과정에서 사용하는 배송지 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long addressId; // 주소 ID (orders.shipping_address_id 등)
    private String addressNickname; // 주소 별칭 (예: 자택, 직장 등)
    private String recipientName; // 수령인 이름
    private String recipientPhone; // 수령인 연락처
    private String postalCode; // 우편번호
    private String addressMain; // 기본 주소
    private String addressDetail1; // 상세 주소1
    private String addressDetail2; // 상세 주소2
    private String addressDetail3; // 상세 주소3
    private String memo; // 배송 메모 (필요시 적용가능)
    private Boolean isDefault; // 기본 배송지 여부
}