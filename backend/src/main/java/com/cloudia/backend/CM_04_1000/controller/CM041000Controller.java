package com.cloudia.backend.CM_04_1000.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cloudia.backend.CM_04_1000.constants.CM041000MessageConstant;
import com.cloudia.backend.CM_04_1000.model.OrderDetailResponse;
import com.cloudia.backend.CM_04_1000.model.ResponseModel;
import com.cloudia.backend.CM_04_1000.model.ReviewInfo;
import com.cloudia.backend.CM_04_1000.model.ReviewRequest;
import com.cloudia.backend.CM_04_1000.service.CM041000Service;
import com.cloudia.backend.config.jwt.JwtTokenProvider;

import org.springframework.util.DigestUtils;

import lombok.RequiredArgsConstructor; 
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/guest")
@CrossOrigin(origins = "*")
public class CM041000Controller {
    // Service 정의
    private final CM041000Service cm041000Service;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 리뷰 전체 목록 조회
     *
     * @return 리뷰 목록
     */
    @GetMapping("/reviews")
    public ResponseEntity<ResponseModel<List<ReviewInfo>>> getAllReviews() {
        try {
            List<ReviewInfo> list = cm041000Service.findAllReviews();
            log.info(CM041000MessageConstant.REVIEW_FETCH_SUCCESS);
            return ResponseEntity.ok(setResponseDto(list, true, CM041000MessageConstant.REVIEW_FETCH_SUCCESS));
        } catch (Exception e) {
            log.error(CM041000MessageConstant.REVIEW_FETCH_FAIL, e);
            return ResponseEntity.internalServerError()
                .body(setResponseDto(null, false, CM041000MessageConstant.REVIEW_FETCH_FAIL));
        }
    }

    /**
     * 리뷰 등록 (이미지 포함)
     */
    @PostMapping("/reviews/upload")
    public ResponseEntity<ResponseModel<Long>> createReviewWithImage(
            @ModelAttribute ReviewRequest entity,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            return cm041000Service.createReviewWithImage(entity, file);
        } catch (Exception e) {
            log.error(CM041000MessageConstant.REVIEW_REGISTER_FAIL, e);
            return ResponseEntity.internalServerError()
                .body(setResponseDto(null, false, CM041000MessageConstant.REVIEW_REGISTER_FAIL));
        }
    }

    /**
     * 리뷰 수정 (이미지 포함)
     */
    @PostMapping("/reviews/update")
    public ResponseEntity<ResponseModel<Boolean>> updateReviewWithImage(
            @ModelAttribute ReviewRequest entity,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            return cm041000Service.updateReviewWithImage(entity, file);
        } catch (Exception e) {
            log.error(CM041000MessageConstant.REVIEW_UPDATE_FAIL, e);
            return ResponseEntity.internalServerError()
                .body(setResponseDto(Boolean.FALSE, false, CM041000MessageConstant.REVIEW_UPDATE_FAIL));
        }
    }

    /**
     * 리뷰 본문 에디터 이미지 업로드
     */
    @PostMapping("/reviews/image/upload")
    public ResponseEntity<ResponseModel<String>> uploadEditorImage(@RequestParam("file") MultipartFile file) {
        return cm041000Service.uploadReviewEditorImage(null, file);
    }

    /**
     * 리뷰 상세 조회
     *
     * @param reviewId 리뷰 ID
     * @return 리뷰 단건
     */
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ResponseModel<ReviewInfo>> getReviewById(@PathVariable Long reviewId) {
        try {
            ReviewInfo review = cm041000Service.findReviewById(reviewId);
            if (review == null) {
                log.warn(CM041000MessageConstant.REVIEW_NOT_FOUND + " reviewId: {}", reviewId);
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(setResponseDto(null, false, CM041000MessageConstant.REVIEW_NOT_FOUND));
            }
            return ResponseEntity.ok(
                setResponseDto(review, true, CM041000MessageConstant.REVIEW_DETAIL_FETCH_SUCCESS));
        } catch (Exception e) {
            log.error("{} reviewId: {}", CM041000MessageConstant.REVIEW_DETAIL_FETCH_FAIL, reviewId, e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(setResponseDto(null, false, CM041000MessageConstant.REVIEW_DETAIL_FETCH_FAIL));
        }
    }

    /**
     * 리뷰 조회수 증가 (하루 1회 제한)
     *
     * @param reviewId 리뷰 ID
     * @return 처리 결과
     */
    @PostMapping("/reviews/{reviewId}/view")
    public ResponseEntity<ResponseModel<Boolean>> increaseViewCount(
            @PathVariable Long reviewId,
            HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            String viewerKey = buildViewerKey(userId, request);
            boolean incremented = cm041000Service.increaseViewOncePerDay(reviewId, userId, viewerKey);

            String message = incremented
                ? CM041000MessageConstant.REVIEW_VIEW_INCREMENT_SUCCESS
                : CM041000MessageConstant.REVIEW_VIEW_INCREMENT_ALREADY_COUNTED;
            return ResponseEntity.ok(setResponseDto(Boolean.valueOf(incremented), true, message));
        } catch (Exception e) {
            log.error("{} reviewId: {}", CM041000MessageConstant.REVIEW_VIEW_INCREMENT_ERROR, reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(setResponseDto(Boolean.FALSE, false, CM041000MessageConstant.REVIEW_VIEW_INCREMENT_FAIL));
        }
    }

    /**
     * 작성자용 주문 + 상품 목록 조회
     *
     * @param memberNumber 회원번호
     * @return 주문 + 상품 목록
     */
    @GetMapping("/reviews/orders")
    public ResponseEntity<ResponseModel<List<OrderDetailResponse>>> getOrdersWithProducts(
            @RequestParam String memberNumber) {
        try {
            List<OrderDetailResponse> list = cm041000Service.findOrdersWithProducts(memberNumber);
            return ResponseEntity.ok(
                setResponseDto(list, true, CM041000MessageConstant.REVIEW_ORDER_FETCH_SUCCESS));
        } catch (Exception e) {
            log.error("주문 + 상품 목록 조회 실패: memberNumber={}", memberNumber, e);
            return ResponseEntity.internalServerError()
                .body(setResponseDto(null, false, CM041000MessageConstant.REVIEW_ORDER_FETCH_FAIL));
        }
    }
    
    /**
     * 리뷰 소프트 딜리트 (본인만 가능)
     *
     * @param reviewId 리뷰 ID
     * @return 소프트 딜리트 결과
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ResponseModel<Integer>> deleteReview(
            @PathVariable Long reviewId,
            HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            return cm041000Service.deleteReview(reviewId, userId);
        } catch (Exception e) {
            log.error(CM041000MessageConstant.REVIEW_DELETE_FAIL + " reviewId: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(setResponseDto(0, false, CM041000MessageConstant.REVIEW_DELETE_FAIL));
        }
    }

    /**
     * 리뷰 이미지 삭제 (본인만 가능, imageId 기반)
     *
     * @param reviewId 리뷰 ID
     * @param imageId 리뷰 이미지 PK
     * @return 삭제 결과
     */
    @DeleteMapping("/reviews/{reviewId}/images/{imageId}")
    public ResponseEntity<ResponseModel<Void>> deleteReviewImage(
            @PathVariable Long reviewId,
            @PathVariable Long imageId) {
        try {
            boolean deleted = cm041000Service.deleteReviewImage(reviewId, imageId);
            if (!deleted) {
                log.warn("리뷰 이미지 삭제 실패 reviewId={}, imageId={}", reviewId, imageId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(setResponseDto(null, false, CM041000MessageConstant.REVIEW_IMAGE_DELETE_FAIL));
            }
            log.info(CM041000MessageConstant.REVIEW_IMAGE_DELETE_SUCCESS + " reviewId={}, imageId={}", reviewId, imageId);
            return ResponseEntity.ok(
                setResponseDto(null, true, CM041000MessageConstant.REVIEW_IMAGE_DELETE_SUCCESS));
        } catch (Exception e) {
            log.error(CM041000MessageConstant.REVIEW_IMAGE_DELETE_FAIL + " reviewId={}, imageId={}", reviewId, imageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(setResponseDto(null, false, CM041000MessageConstant.REVIEW_IMAGE_DELETE_FAIL));
        }
    }

    /**
     * 리뷰 메인 이미지 업로드
     *
     * @param reviewId 리뷰 ID
     * @param file 업로드할 이미지
     * @return 이미지 업로드에 성공하면 업로드된 이미지 URL, 실패 시 null
     */
    @PostMapping("/reviews/{reviewId}/image")
    public ResponseEntity<ResponseModel<String>> uploadReviewMainImage(
            @PathVariable Long reviewId,
            @RequestParam("file") MultipartFile file) {
        try {
            return cm041000Service.uploadReviewMainImage(reviewId, file);
        } catch (Exception e) {
            log.error("{} reviewId: {}", CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_FAIL, reviewId, e);
            return ResponseEntity.internalServerError()
                .body(setResponseDto(null, false, CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_FAIL));
        }
    }

    /**
     * JWT 토큰에서 사용자 ID를 추출
     *
     * @param request HTTP 요청 객체
     * @return 사용자 ID, 토큰이 없거나 유효하지 않으면 null 반환
     */
    private Long extractUserId(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token == null) {
            return null;
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        if (userId == null) {
            log.debug("Failed to extract userId from JWT token");
        }
        return userId;
    }

    /**
     * 뷰어 키를 생성. 로그인한 사용자는 "user:{userId}" 형식,
     * 비로그인 사용자는 클라이언트 IP 주소의 MD5 해시를 이용한 "guest:{hashedIp}" 
     * 형식으로 생성
     * @param userId  사용자 ID (로그인 상태일 경우)
     * @param request HTTP 요청 객체
     * @return 뷰어 키 문자열
     */
    private String buildViewerKey(Long userId, HttpServletRequest request) {
        if (userId != null) {
            return "user:" + userId;
        }
        String clientIp = resolveClientIp(request);
        String hashedIp = DigestUtils.md5DigestAsHex(clientIp.getBytes(StandardCharsets.UTF_8));
        return "guest:" + hashedIp;
    }

    /**
     * 클라이언트의 실제 IP 주소를 추출
     * "X-Forwarded-For" 헤더가 있으면 첫 번째 IP를 사용하고,
     * 없으면 요청의 원격 주소를 반환
     * @param request HTTP 요청 객체
     * @return 클라이언트 IP 주소 문자열
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 공통 응답 포맷 설정
     *
     * @param resultList 결과 데이터
     * @param ret        성공 여부
     * @param msg        메시지
     * @return 공통 응답 모델
     */
    private <T> ResponseModel<T> setResponseDto(T resultList, boolean ret, String msg) {
        return ResponseModel.<T>builder()
            .resultList(resultList)
            .result(ret)
            .message(msg)
            .build();
    }
}
