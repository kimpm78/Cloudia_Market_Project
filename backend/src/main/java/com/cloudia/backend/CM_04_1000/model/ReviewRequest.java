package com.cloudia.backend.CM_04_1000.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {
    private Long userId;       // 작성자 ID
    private String memberNumber;   // 주문 회원번호
    private String orderNumber;    // 주문 번호
    private Long reviewId;         // 등록 후 PK 세팅용

    @NotBlank(message = "상품 코드를 입력해주세요.")
    private String productCode;  // 프론트에서 전달하는 상품 코드
    private Long productId;      // 서버에서 productCode → productId 변환 후 세팅 (VARCHAR)

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;      // 제목
    private Integer reviewType; // 0=리뷰, 1=후기
    private String imageUrl;   // 첨부 이미지

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;    // 본문

    private String createdBy;   // 서버에서 자동 세팅
    private String updatedBy;   // 서버에서 자동 세팅
}