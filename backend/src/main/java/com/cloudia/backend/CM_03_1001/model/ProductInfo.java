package com.cloudia.backend.CM_03_1001.model;

import java.time.LocalDateTime;
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
public class ProductInfo {
    private Long productId; // 상품ID
    private String productCode; // 상품 코드
    private String name; // 상품명
    private int price; // 상품 가격
    private int deliveryPrice; // 배달비
    private String releaseDate; // 출고일
    private String reservationDeadline; // 예약마감일
    private String estimatedDeliveryDate; // 상시판매 예상 배송일(서버 계산)
    private Boolean isReservationClosed; // 예약 마감 여부 (서버 계산/배치)
    private Boolean isSoldOut; // 품절 여부 (서버 계산)
    private int codeValue; // 코드 값
    private String category; // 카테고리
    private String categoryGroupName; // 카테고리 그룹 명
    private String createdBy; // 등록자
    private LocalDateTime createdAt; // 등록일
    private String updatedBy; // 수정자
    private LocalDateTime updatedAt; // 수정일
    private String thumbnailUrl; // 썸네일 이미지
    private String description; // 상품 설명
    private Double weight; // 중량
    private Integer availableQty; // 가용 재고 수량
    private Integer purchaseLimit; // 최대 구매 수량
    private List<String> detailImages; // 상세 이미지 리스트
}
