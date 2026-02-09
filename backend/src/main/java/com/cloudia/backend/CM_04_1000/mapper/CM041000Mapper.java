package com.cloudia.backend.CM_04_1000.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_04_1000.model.Attachments;
import com.cloudia.backend.CM_04_1000.model.OrderDetailResponse;
import com.cloudia.backend.CM_04_1000.model.ReviewInfo;
import com.cloudia.backend.CM_04_1000.model.ReviewRequest;

@Mapper
public interface CM041000Mapper {

    // ================================
    // 리뷰 CRUD 관련 메서드
    // ================================

    /**
     * 리뷰 목록 조회 (상품별, 페이지네이션 포함)
     *
     * @param params 파라미터 맵 (productId, size, offset 등)
     * @return 리뷰 리스트
     */
    List<ReviewInfo> selectReviews(java.util.Map<String, Object> params);

    /**
     * 리뷰 단건 조회
     *
     * @param reviewId 리뷰 ID
     * @return 리뷰 상세 정보
     */
    ReviewInfo selectReviewById(@Param("reviewId") Long reviewId);

    /**
     * 리뷰 작성
     *
     * @param review 작성할 리뷰 요청 정보
     * @return 생성된 리뷰 ID
     */
    Long writeReview(ReviewRequest review);

    /**
     * 리뷰 수정
     *
     * @param reviewId 수정할 리뷰 ID
     * @param review 수정할 리뷰 요청 정보
     * @return 수정된 행 수 (성공: 1, 실패: 0)
     */
    int updateReview(@Param("reviewId") Long reviewId, 
        @Param("review") ReviewRequest review);

    /**
     * 리뷰 삭제
     *
     * @param reviewId 삭제할 리뷰 ID
     * @param userId 삭제를 요청한 사용자 ID
     * @return 삭제된 행 수
     */
    int deleteReview(@Param("reviewId") Long reviewId, @Param("userId") Long userId);

    /**
     * 리뷰 조회수 +1
     *
     * @param reviewId 조회수 증가 대상 리뷰 ID
     */
    int incrementViewCount(@Param("reviewId") Long reviewId);

    // ================================
    // 주문 및 상품 관련 메서드
    // ================================

    /**
     * 작성자용 주문 + 상품 목록 조회
     *
     * @param memberNumber 회원 번호
     * @return 주문 + 상품 목록
     */
    List<OrderDetailResponse> selectOrdersWithProducts(@Param("memberNumber") String memberNumber);

    /**
     * 주문 내 상품 존재 여부 확인 (리뷰 작성 전 검증)
     *
     * @param memberNumber 회원 번호
     * @param orderNumber 주문 번호
     * @param productCode 상품 코드
     * @return 존재하면 1 이상, 없으면 0
     */
    int checkOrderProduct(@Param("memberNumber") String memberNumber,
        @Param("orderNumber") String orderNumber,
        @Param("productCode") String productCode);

    /**
     * productCode로 productId 조회 (리뷰 등록 시 변환용)
     *
     * @param productCode 상품 코드
     * @return 상품 PK
     */
    Long findProductIdByCode(@Param("productCode") String productCode);

    // ================================
    // 이미지 관리 관련 메서드
    // ================================

    /**
     * 리뷰 메인 이미지 업데이트
     *
     * @param reviewId 리뷰 ID
     * @param imageUrl 메인 이미지 URL
     * @return 성공한 행 수
     */
    int updateReviewImage(@Param("reviewId") Long reviewId, @Param("imageUrl") String imageUrl);
        
    /**
     * 리뷰 에디터 이미지 등록
     *
     * @param attachment 등록할 첨부 이미지 정보
     * @return 등록된 행 수
     */
    int insertReviewAttachment(Attachments attachment);

    /**
     * 리뷰 에디터 이미지 수정
     *
     * @param attachment 수정할 첨부 이미지 정보
     * @return 수정된 행 수
     */
    // int updateReviewAttachment(Attachments attachment);

    /**
     * 리뷰 에디터 이미지 조회
     *
     * @param reviewId 리뷰 ID
     * @return 첨부 이미지 리스트
     */
    List<Attachments> selectReviewAttachments(@Param("reviewId") Long reviewId);

    /**
     * 리뷰 에디터 이미지 삭제
     *
     * @param imageId 첨부 이미지 ID
     * @param reviewId 리뷰 ID
     * @return 삭제된 행 수
     */
    int deleteReviewAttachment(@Param("imageId") Long imageId, @Param("reviewId") Long reviewId);

    // ================================
    // 리뷰 본문 내용 업데이트 관련 메서드
    // ================================

    /**
     * 리뷰 본문 내용 업데이트
     *
     * @param reviewId 리뷰 ID
     * @param content  수정된 HTML 콘텐츠
     * @param updatedBy 수정자
     * @return 업데이트된 행 수
     */
    int updateReviewContent(@Param("reviewId") Long reviewId,
        @Param("content") String content,
        @Param("updatedBy") String updatedBy);

}