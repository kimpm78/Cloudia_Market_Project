package com.cloudia.backend.CM_04_1000.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.cloudia.backend.CM_04_1000.model.OrderDetailResponse;
import com.cloudia.backend.CM_04_1000.model.ResponseModel;
import com.cloudia.backend.CM_04_1000.model.ReviewInfo;
import com.cloudia.backend.CM_04_1000.model.ReviewRequest;

public interface CM041000Service {

    /**
     * 리뷰 단건 조회
     *
     * @param reviewId 리뷰 ID
     * @return 리뷰 정보 (없으면 null)
     */
    ReviewInfo findReviewById(Long reviewId);

    /**
     * 상품별 리뷰 목록 조회 (페이지네이션)
     *
     * @param productId 상품 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 리뷰 리스트
     */
    List<ReviewInfo> findReviewsByProduct(Long productId, Integer page, Integer size);

    /**
     * 리뷰 전체 목록 조회
     *
     * @return 리뷰 리스트
     */
    List<ReviewInfo> findAllReviews();

    /**
     * 리뷰 삭제 (소프트 삭제)
     *
     * @param reviewId 리뷰 ID
     * @param userId 로그인 사용자 ID
     * @return 삭제 결과 (ResponseModel 포함, 삭제된 개수, 성공 여부, 메시지 포함)
     */
    ResponseEntity<ResponseModel<Integer>> deleteReview(Long reviewId, Long userId);

    /**
     * 리뷰 조회수 증가 (하루 1회 제한)
     *
     * @param reviewId 리뷰 ID
     * @param userId 로그인 사용자 ID (비로그인 시 null)
     * @param viewerKey 중복 체크용 식별자 (userId 또는 비로그인 IP 기반)
     * @return 조회수가 증가했으면 true, 제한으로 건너뛰었으면 false
     */
    boolean increaseViewOncePerDay(Long reviewId, Long userId, String viewerKey);

    /**
     * 특정 회원의 주문 + 상품 목록 조회
     *
     * @param memberNumber 회원 번호
     * @return 주문 + 상품 목록 (주문 단위, products 리스트 포함)
     */
    List<OrderDetailResponse> findOrdersWithProducts(String memberNumber);

    /**
     * 주문 내 상품이 실제 존재하는지 검증
     *
     * @param memberNumber 회원 번호
     * @param orderNumber 주문 번호
     * @param productCode 상품 코드
     * @return 존재하면 true, 없으면 false
     */
    boolean checkOrderProduct(String memberNumber, String orderNumber, String productCode);

    /**
     * 리뷰 등록 (MultipartFile 포함)
     *
     * @param entity 리뷰 요청 정보
     * @param file 업로드할 이미지
     * @return 등록 결과 (ResponseModel 포함, 생성된 리뷰 ID, 성공 여부, 메시지 포함)
     */
    ResponseEntity<ResponseModel<Long>> createReviewWithImage(ReviewRequest entity, MultipartFile file);

    /**
     * 리뷰 수정 (MultipartFile 포함)
     *
     * @param entity 리뷰 요청 정보
     * @param file 업로드할 이미지
     * @return 수정 성공 여부 (true: 성공, false: 실패)
     */
    ResponseEntity<ResponseModel<Boolean>> updateReviewWithImage(ReviewRequest entity, MultipartFile file);

    /**
     * 리뷰 본문 에디터 이미지 업로드 (임시/수정 공용)
     *
     * @param reviewId 리뷰 ID (null 또는 0이면 임시 업로드)
     * @param file 업로드할 이미지
     * @return 업로드된 이미지 URL 포함 ResponseModel
     */
    ResponseEntity<ResponseModel<String>> uploadReviewEditorImage(Long reviewId, MultipartFile file);

    /**
     * 리뷰 메인 이미지 업로드 (썸네일)
     *
     * @param reviewId 리뷰 ID
     * @param file 업로드할 메인 이미지
     * @return 업로드된 이미지 URL
     */
    ResponseEntity<ResponseModel<String>> uploadReviewMainImage(Long reviewId, MultipartFile file);

    /**
     * 리뷰 에디터 이미지 삭제 (imageId 기반)
     *
     * @param reviewId 리뷰 ID
     * @param imageId 리뷰 이미지 PK
     * @return 삭제 성공 여부
     */
    boolean deleteReviewEditorImage(Long reviewId, Long imageId);

    /**
     * 리뷰 이미지 삭제 (imageId 기반)
     *
     * @param reviewId 리뷰 ID
     * @param imageId 리뷰 이미지 PK
     * @return 삭제 성공 여부
     */
    boolean deleteReviewImage(Long reviewId, Long imageId);
}