package com.cloudia.backend.CM_90_1052.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
public class RefundRequestDto {
    /**
     * 요청 번호
     */
    private String requestNo;

    /**
     * 회원 번호
     */
    private String refundNumber;

    /**
     * 구매 번호
     */
    private String orderNumber;

    /**
     * 환불 타입 (0: 환불, 1: 교환)
     */
    private String refundType;

    /**
     * 교환 부품 (0: 풀패키지, 1: 본체, 2: 파츠)
     */
    private String exchangeParts;

    /**
     * 상품 리스트
     */
    private List<RefundProductDto> products;

    /**
     * 배송비 부담 (0: 무기와라 장터, 1: 구매자)
     */
    private String shippingFee;

    /**
     * 배송 금액
     */
    private Integer shippingAmount;

    /**
     * 취소 상품 총 금액
     */
    private Integer productTotalAmount;

    /**
     * 총 취소 금액
     */
    private Integer totalAmount;

    /**
     * 메모
     */
    private String memo;

    /**
     * 환불 상품 상세 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundProductDto {

        /**
         * 상품 번호
         */
        private String productNumber;

        /**
         * 상품명
         */
        private String productName;

        /**
         * 수량
         */
        private Integer quantity;

        /**
         * 단가
         */
        private Integer unitPrice;

        /**
         * 총 금액
         */
        private Integer totalPrice;
    }
}
