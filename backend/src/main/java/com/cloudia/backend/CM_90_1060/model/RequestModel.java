package com.cloudia.backend.CM_90_1060.model;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

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
public class RequestModel {
    private long productId;
    private String categoryGroup;// 카테고리
    private String productCode;// 상품 코드
    private String productName;// 상품 명
    private int productPrice;// 상품 가격
    private int shippingFee;// 배송비
    private String purchasePrice;// 사입가
    private String expectedDeliveryDate;// 출고 예정일
    private String reservationDeadline; // 예약 마감일
    private MultipartFile productFile;// 상품 이미지
    private MultipartFile[] detailImages; // 상세 이미지
    private String productnote;// 상품 설명
    private int purchaseLimit; // 구매 수량
    private double weight; // 상품 무게
    private List<String> existingDetailImages;
    private List<String> deletedDetailImages;
}
