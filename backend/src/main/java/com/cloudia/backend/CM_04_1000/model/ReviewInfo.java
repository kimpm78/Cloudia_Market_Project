package com.cloudia.backend.CM_04_1000.model;

import com.cloudia.backend.CM_04_1001.model.ReviewCommentInfo;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

/**
 * 리뷰 + 상세/목록 조회에 사용하는 응답
 */
@Getter
@Setter
public class ReviewInfo {
    private Long reviewId;        // 리뷰 ID
    private Long userId;          // 작성자 ID
    private Long productId;       // 상품 ID
    private String productName;   // 상품 이름 추가
    private Long orderId;         // 주문 ID
    private String orderNumber;   // 주문번호 (예: '00001')
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;     // 주문일자 (YYYY-MM-DD)
    private String productCode; // 주문상세 코드 (예: A10001)
    
    private Integer reviewType; // 0 = 리뷰, 1 = 후기
    private String title;         // 리뷰 제목
    private String imageUrl;      // 첨부 이미지
    private String content;       // 리뷰 내용
    private Integer viewCount = 0;    // 조회수
    private String createdBy;     // 등록자
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;     // 등록일
    private String updatedBy;     // 수정자
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;     // 수정일

    private java.util.List<ReviewCommentInfo> comments; // 댓글 목록
}