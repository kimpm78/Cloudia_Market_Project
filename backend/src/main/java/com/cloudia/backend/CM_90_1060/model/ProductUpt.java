package com.cloudia.backend.CM_90_1060.model;

import java.util.List;

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
public class ProductUpt {
    private String category;// 카테고리
    private String productCode;// 상품 코드
    private String productCategory; // 상품 분류
    private String productName;// 상품 명
    private int productPrice;// 상품 가격
    private int deliveryPrice;// 배송비
    private String purchasePrice;// 사입가
    private int purchaseLimit; // 구매 수량
    private String expectedDeliveryDate;// 출고 예정일
    private String reservationDeadline; // 예약 마감일
    private String productFile;// 상품 이미지
    private List<String> detailImages; // 상세 이미지
    private String productnote;// 상품 설명
    private Double weight;// 중량
    private int availableQty; // 가용 재고
}
