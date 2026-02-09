
package com.cloudia.backend.CM_04_1001.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ReviewInfo {
    private Long reviewId;       // 리뷰 ID
    private Long productId;      // 상품 ID
    private Long userId;         // 작성자 ID
    private String userName;     // 작성자명
    private String title;        // 리뷰 제목
    private String content;      // 리뷰 내용
    private String createdAt;    // 작성일
    private String updatedAt; // 수정일
    
    // 상세 조회 시 포함되는 댓글/대댓글 목록
    private List<ReviewCommentInfo> comments;
}
