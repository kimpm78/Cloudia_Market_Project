package com.cloudia.backend.CM_04_1000.service.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudia.backend.CM_04_1000.constants.CM041000MessageConstant;
import com.cloudia.backend.CM_04_1000.mapper.CM041000Mapper;
import com.cloudia.backend.CM_04_1000.model.Attachments;
import com.cloudia.backend.CM_04_1000.model.OrderDetailResponse;
import com.cloudia.backend.CM_04_1000.model.ResponseModel;
import com.cloudia.backend.CM_04_1000.model.ReviewInfo;
import com.cloudia.backend.CM_04_1000.model.ReviewRequest;
import com.cloudia.backend.CM_04_1000.service.CM041000Service;
import com.cloudia.backend.CM_04_1001.mapper.CM041001Mapper;
import com.cloudia.backend.common.service.S3Service;
import com.cloudia.backend.config.RedisUtils;
import com.cloudia.backend.constants.CMMessageConstant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.dao.DataAccessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM041000ServiceImpl implements CM041000Service {
    // Mapper 정의
    private final CM041000Mapper cm041000Mapper;
    private final CM041001Mapper cm041001Mapper;
    private static final long VIEW_CACHE_TTL_MS = TimeUnit.DAYS.toMillis(1);
    private final RedisUtils redisUtils;

    @Value("${uuid.upload.dir}")
    private String uuidDir;
    
    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.type:}")
    private String uploadType;

    @Autowired(required = false)
    private S3Service s3Service;
    
    @Value("${app.upload.s3.base-url:}")
    private String baseUrl;

    /**
     * 리뷰 삭제
     *
     * @param reviewId 삭제할 리뷰 ID
     * @return 삭제 결과
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Integer>> deleteReview(Long reviewId, Long userId) {
        if (reviewId == null || reviewId <= 0) {
            log.warn("{} reviewId={}", CM041000MessageConstant.REVIEW_DELETE_FAIL, reviewId);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(0, false, CM041000MessageConstant.REVIEW_DELETE_FAIL));
        }
        if (userId == null || userId <= 0) {
            log.warn("{} reviewId={}, userId={}", CMMessageConstant.FAIL_UNAUTHORIZED, reviewId, userId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createResponseModel(0, false, CMMessageConstant.FAIL_UNAUTHORIZED));
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String loginId = (authentication != null) ? authentication.getName() : null;

            // 관련 댓글/대댓글 하드 삭제
            try {
                cm041001Mapper.deleteCommentsByReviewId(reviewId);
            } catch (Exception e) {
                log.warn("리뷰 삭제 시 댓글 삭제 실패 reviewId={}, error={}", reviewId, e.getMessage(), e);
            }

            int affected = cm041000Mapper.deleteReview(reviewId, userId);
            if (affected > 0) {
                log.info("{} reviewId={}, userId={}, loginId={}",
                        CM041000MessageConstant.REVIEW_DELETE_SUCCESS, reviewId, userId, loginId);
                return ResponseEntity.ok(
                        createResponseModel(affected, true, CM041000MessageConstant.REVIEW_DELETE_SUCCESS));
            } else {
                log.warn("{} reviewId={}, userId={}", CM041000MessageConstant.REVIEW_FORBIDDEN, reviewId, userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createResponseModel(0, false, CM041000MessageConstant.REVIEW_FORBIDDEN));
            }
        } catch (DataAccessException dae) {
            log.error(CM041000MessageConstant.REVIEW_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_DATABASE));
        } catch (Exception e) {
            log.error(CM041000MessageConstant.REVIEW_DELETE_FAIL, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 특정 상품의 리뷰 목록 조회 (옵션 페이지네이션)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReviewInfo> findReviewsByProduct(Long productId, Integer page, Integer size) {
        try {
            Integer offset = (page != null && size != null) ? page * size : null;
            Map<String, Object> params = new HashMap<>();
            params.put("productId", productId);
            params.put("size", size);
            params.put("offset", offset);
            List<ReviewInfo> reviews = cm041000Mapper.selectReviews(params);
            log.info(CM041000MessageConstant.REVIEW_FETCH_SUCCESS);
            return reviews;
        } catch (Exception e) {
            log.error(CM041000MessageConstant.REVIEW_DB_ERROR, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 리뷰 단건 조회
     */
    @Override
    @Transactional(readOnly = false)
    public ReviewInfo findReviewById(Long reviewId) {
        try {
            ReviewInfo review = cm041000Mapper.selectReviewById(reviewId);
            log.info(CM041000MessageConstant.REVIEW_FETCH_SUCCESS);
            return review;
        } catch (Exception e) {
            log.error(CM041000MessageConstant.REVIEW_DB_ERROR, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 리뷰 전체 목록 조회 (productId 없이 전체)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReviewInfo> findAllReviews() {
        return findReviewsByProduct(null, null, null);
    }



    /**
     * 리뷰 조회수 1일 1회 증가 처리
     *
     * @param reviewId 리뷰 ID
     * @param userId   사용자 ID
     */
    @Override
    @Transactional
    public boolean increaseViewOncePerDay(Long reviewId, Long userId, String viewerKey) {
        if (viewerKey == null || viewerKey.isBlank()) {
            log.warn("Viewer key is missing. reviewId={}, userId={}", reviewId, userId);
            return false;
        }

        final String cacheKey = String.format("review:view:%d:%s", reviewId, viewerKey);
        boolean firstVisit = redisUtils.setIfAbsent(cacheKey, "Y", VIEW_CACHE_TTL_MS);
        if (!firstVisit) {
            log.debug("{} key={}", CM041000MessageConstant.REVIEW_VIEW_ALREADY_COUNTED, cacheKey);
            return false;
        }

        try {
            int updatedRows = cm041000Mapper.incrementViewCount(reviewId);
            if (updatedRows > 0) {
                log.info("{} reviewId={}, userId={}",
                    CM041000MessageConstant.REVIEW_VIEW_INCREMENT_SUCCESS, reviewId, userId);
                return true;
            }

            redisUtils.deleteData(cacheKey);
            log.warn("{} reviewId={}, userId={}",
                CM041000MessageConstant.REVIEW_VIEW_INCREMENT_FAIL, reviewId, userId);
            return false;
        } catch (Exception e) {
            redisUtils.deleteData(cacheKey);
            log.error("{} reviewId={}, userId={}",
                CM041000MessageConstant.REVIEW_VIEW_INCREMENT_ERROR, reviewId, userId, e);
            throw e;
        }
    }

    /**
     * 주문 + 상품 목록 조회
     *
     * @param memberNumber 회원 번호
     * @return 주문 상세 내역 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDetailResponse> findOrdersWithProducts(String memberNumber) {
        return cm041000Mapper.selectOrdersWithProducts(memberNumber);
    }

    /**
     * 주문 내역에 해당 상품이 존재하는지 검증
     *
     * @param memberNumber 회원 번호
     * @param orderNumber  주문 번호
     * @param productCode  상품 코드
     * @return true 존재함, false 존재하지 않음
     */
    @Override
    @Transactional(readOnly = true)
    public boolean checkOrderProduct(String memberNumber, String orderNumber, String productCode) {
        try {
            int count = cm041000Mapper.checkOrderProduct(memberNumber, orderNumber, productCode);
            boolean exists = count > 0;
            if (exists) {
                log.info(CM041000MessageConstant.REVIEW_ORDER_PRODUCT_FOUND +
                        " memberNumber={}, orderNumber={}, productCode={}",
                        memberNumber, orderNumber, productCode);
            } else {
                log.warn(CM041000MessageConstant.REVIEW_ORDER_PRODUCT_NOT_FOUND +
                        " memberNumber={}, orderNumber={}, productCode={}",
                        memberNumber, orderNumber, productCode);
            }
            return exists;
        } catch (Exception e) {
            log.error(CM041000MessageConstant.REVIEW_ORDER_PRODUCT_ERROR +
                    " memberNumber={}, orderNumber={}, productCode={}",
                    memberNumber, orderNumber, productCode, e);
            throw e;
        }
    }

    /**
     * 리뷰 메인 이미지 업로드
     *
     * @param reviewId 리뷰 ID
     * @param file 업로드할 이미지
     * @return 업로드된 이미지 URL
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<String>> uploadReviewMainImage(Long reviewId, MultipartFile file) {
        log.info("Start uploadReviewMainImage: reviewId={}", reviewId);
        try {
            ResponseEntity<ResponseModel<String>> validationResponse = validateReviewInput(reviewId, file);
            if (validationResponse != null) return validationResponse;
            return saveMainImage(reviewId, file);
        } catch (DataAccessException dae) {
            return handleError(
                String.format("%s reviewId=%s, error=%s", CM041000MessageConstant.REVIEW_DB_ERROR, reviewId, dae.getMessage()),
                dae,
                CMMessageConstant.ERROR_DATABASE
            );
        } catch (Exception e) {
            return handleError(
                String.format("%s reviewId=%s, error=%s", CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_FAIL, reviewId, e.getMessage()),
                e,
                CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_FAIL
            );
        }
    }

    /**
     * 리뷰 본문 에디터 이미지 임시 업로드 (리뷰 생성/수정 시 이동)
     *
     * @param reviewId 리뷰 ID (null 또는 0이면 임시 업로드)
     * @param file 업로드할 이미지
     * @return 업로드된 이미지 URL
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<String>> uploadReviewEditorImage(Long reviewId, MultipartFile file) {
        log.info("Start uploadReviewEditorImage: reviewId={}", reviewId);
        try {
            if (file == null || file.isEmpty()) {
                log.warn("{} reviewId={}", CM041000MessageConstant.REVIEW_VALIDATION_FAIL, reviewId);
                return ResponseEntity.badRequest()
                        .body(createResponseModel(null, false, CM041000MessageConstant.REVIEW_VALIDATION_FAIL));
            }
            String savedName = saveFile(file, "tmp");
            String filePath = "/tmp/" + savedName;
            try {
                cleanupOldTmpImages();
            } catch (Exception cleanupEx) {
                log.debug("임시 이미지 정리 중 경고 reviewId={}, error={}", reviewId, cleanupEx.getMessage());
            }
            log.info("{} reviewId={}, path={}", CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_SUCCESS, reviewId, filePath);
            log.info("End uploadReviewEditorImage: reviewId={}", reviewId);
            return ResponseEntity.ok(createResponseModel(filePath, true, CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_SUCCESS));
        } catch (DataAccessException dae) {
            log.error("{} reviewId={}, error={}", CM041000MessageConstant.REVIEW_DB_ERROR, reviewId, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(null, false, CMMessageConstant.ERROR_DATABASE));
        } catch (Exception e) {
            log.error("{} reviewId={}, error={}", CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_FAIL, reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(null, false, CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_FAIL));
        }
    }

    /**
     * 리뷰 생성 + 메인/에디터 이미지 처리
     */
    @Transactional
    public ResponseEntity<ResponseModel<Long>> createReviewWithImage(ReviewRequest review, MultipartFile file) {
        log.info("Start createReviewWithImage");
        try {
            String loginId = resolveLoginId(review != null ? review.getCreatedBy() : null);
            if (review != null) {
                review.setCreatedBy(loginId);
                review.setUpdatedBy(loginId);
            }
            // Validation 및 상품/주문 체크
            Integer reviewType = review != null ? review.getReviewType() : null;
            boolean isReviewType = Objects.equals(reviewType, 0); // 0=리뷰, 1=후기
            boolean requireProduct = !isReviewType; // 후기일 때만 상품 필수
            boolean hasProductCode = StringUtils.hasText(review.getProductCode());

            if (requireProduct && !hasProductCode) {
                log.warn("{} 상품 코드가 없습니다. memberNumber={}, orderNumber={}, productCode={}",
                        CM041000MessageConstant.REVIEW_VALIDATION_FAIL,
                        review.getMemberNumber(), review.getOrderNumber(), review.getProductCode());
                return ResponseEntity.badRequest()
                        .body(createResponseModel(null, false, CM041000MessageConstant.REVIEW_VALIDATION_FAIL));
            }

            boolean hasOrderInfo = StringUtils.hasText(review.getMemberNumber()) && StringUtils.hasText(review.getOrderNumber());
            if (hasOrderInfo && hasProductCode) {
                boolean orderProductExists = false;
                try {
                    orderProductExists = checkOrderProduct(review.getMemberNumber(), review.getOrderNumber(), review.getProductCode());
                } catch (Exception e) {
                    log.error("{} memberNumber={}, orderNumber={}, productCode={}, error={}",
                            CM041000MessageConstant.REVIEW_ORDER_FETCH_FAIL,
                            review.getMemberNumber(), review.getOrderNumber(), review.getProductCode(), e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createResponseModel(null, false, CM041000MessageConstant.REVIEW_ORDER_FETCH_FAIL));
                }
                if (!orderProductExists) {
                    log.warn("{} memberNumber={}, orderNumber={}, productCode={}",
                            CM041000MessageConstant.REVIEW_ORDER_FETCH_FAIL,
                            review.getMemberNumber(), review.getOrderNumber(), review.getProductCode());
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(createResponseModel(null, false, CM041000MessageConstant.REVIEW_ORDER_FETCH_FAIL));
                }
            } else if (!hasProductCode) {
                // 리뷰 타입(0)이고 상품/주문을 선택하지 않은 경우 DB NOT NULL 회피용 기본값
                review.setProductCode("");
                if (!StringUtils.hasText(review.getOrderNumber())) {
                    review.setOrderNumber("");
                }
            } else {
                log.info("주문번호 미포함 옵션 리뷰 경로 - 주문 검증 건너뜀 memberNumber={}, orderNumber={}",
                        review.getMemberNumber(), review.getOrderNumber());
            }
            Long productId = null;
            if (hasProductCode) {
                try {
                    productId = cm041000Mapper.findProductIdByCode(review.getProductCode());
                } catch (Exception e) {
                    log.error("{} productCode={}, error={}", CM041000MessageConstant.REVIEW_DB_ERROR, review.getProductCode(), e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createResponseModel(null, false, CM041000MessageConstant.REVIEW_DB_ERROR));
                }
                if (productId == null) {
                    if (requireProduct) {
                        log.error("{} productCode={}", CM041000MessageConstant.REVIEW_DB_ERROR, review.getProductCode());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(createResponseModel(null, false, CM041000MessageConstant.REVIEW_VALIDATION_FAIL));
                    } else {
                        // 리뷰 타입이고 상품코드를 못 찾으면 0으로 채움
                        productId = 0L;
                    }
                }
            } else {
                productId = 0L;
            }
            review.setProductId(productId);
            try {
                cm041000Mapper.writeReview(review);
            } catch (Exception e) {
                log.error("{} error={}", CM041000MessageConstant.REVIEW_DB_ERROR, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createResponseModel(null, false, CM041000MessageConstant.REVIEW_DB_ERROR));
            }
            Long reviewId = review.getReviewId();
            if (reviewId == null) {
                log.error("{} reviewId is null after insert", CM041000MessageConstant.REVIEW_UNEXPECTED_ERROR);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createResponseModel(null, false, CM041000MessageConstant.REVIEW_UNEXPECTED_ERROR));
            }
            // 메인 이미지 저장 및 경로 업데이트
            if (file != null && !file.isEmpty()) {
                ResponseEntity<ResponseModel<String>> mainImgResp = saveMainImage(reviewId, file);
                if (mainImgResp.getBody() == null || !mainImgResp.getBody().isResult()) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createResponseModel(null, false, CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_FAIL));
                }
            }
            // 에디터 이미지 처리 및 DB 등록
            ResponseEntity<ResponseModel<Integer>> editorResp = processAndRegisterEditorImages(reviewId, review, loginId);
            if (editorResp != null && (editorResp.getBody() == null || !editorResp.getBody().isResult())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createResponseModel(null, false, CM041000MessageConstant.REVIEW_WRITE_FAIL));
            }
            log.info("{} reviewId={}", CM041000MessageConstant.REVIEW_WRITE_SUCCESS, reviewId);
            log.info("End updateReviewWithImage: reviewId={}", reviewId);
            return ResponseEntity.ok(createResponseModel(reviewId, true, CM041000MessageConstant.REVIEW_WRITE_SUCCESS));
        } catch (DataAccessException dae) {
            return handleError(
                String.format("%s error=%s", CM041000MessageConstant.REVIEW_DB_ERROR, dae.getMessage()),
                dae,
                CMMessageConstant.ERROR_DATABASE
            );
        } catch (Exception e) {
            return handleError(
                String.format("%s error=%s", CM041000MessageConstant.REVIEW_WRITE_FAIL, e.getMessage()),
                e,
                CM041000MessageConstant.REVIEW_WRITE_FAIL
            );
        }
    }

    /**
     * 리뷰 수정 + 메인/에디터 이미지 처리
     */
    @Transactional
    public ResponseEntity<ResponseModel<Boolean>> updateReviewWithImage(ReviewRequest req, MultipartFile file) {
        Long reviewId = req != null ? req.getReviewId() : null;
        log.info("Start updateReviewWithImage: reviewId={}", reviewId);
        try {
            String loginId = resolveLoginId(req != null ? req.getUpdatedBy() : null);
            if (req != null) {
                req.setUpdatedBy(loginId);
            }
            ResponseEntity<ResponseModel<String>> validationResponse = validateReviewInput(reviewId, file);
            if (reviewId == null || reviewId <= 0) {
                log.warn("{} reviewId={}", CM041000MessageConstant.REVIEW_VALIDATION_FAIL, reviewId);
                return ResponseEntity.badRequest()
    		                .body(createResponseModel(false, false, CM041000MessageConstant.REVIEW_VALIDATION_FAIL));
            }
            if (validationResponse != null && validationResponse.getBody() != null && !validationResponse.getBody().isResult()) {
                return ResponseEntity.badRequest()
                        .body(createResponseModel(false, false, CM041000MessageConstant.REVIEW_VALIDATION_FAIL));
            }
            // 메인 이미지 저장 및 경로 업데이트
            if (file != null && !file.isEmpty()) {
                ResponseEntity<ResponseModel<String>> mainImgResp = saveMainImage(reviewId, file);
                if (mainImgResp.getBody() == null || !mainImgResp.getBody().isResult()) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createResponseModel(false, false, CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_FAIL));
                }
            }
            // 에디터 이미지 처리 및 DB 등록
            ResponseEntity<ResponseModel<Integer>> editorResp = processAndRegisterEditorImages(reviewId, req, loginId);
            if (editorResp != null && (editorResp.getBody() == null || !editorResp.getBody().isResult())) {
                log.warn("에디터 이미지 처리 실패 reviewId={}", reviewId);
            }
            int affected = cm041000Mapper.updateReview(reviewId, req);
            if (affected > 0) {
                log.info("{} reviewId={}", CM041000MessageConstant.REVIEW_UPDATE_SUCCESS, reviewId);
                log.info("End updateReviewWithImage: reviewId={}", reviewId);
                return ResponseEntity.ok(createResponseModel(true, true, CM041000MessageConstant.REVIEW_UPDATE_SUCCESS));
            } else {
                log.warn("{} reviewId={}", CM041000MessageConstant.REVIEW_UPDATE_FAIL, reviewId);
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                        .body(createResponseModel(false, false, CM041000MessageConstant.REVIEW_UPDATE_FAIL));
            }
        } catch (DataAccessException dae) {
            return handleError(
                String.format("%s reviewId=%s, error=%s", CM041000MessageConstant.REVIEW_DB_ERROR, reviewId, dae.getMessage()),
                dae,
                CMMessageConstant.ERROR_DATABASE
            );
        } catch (Exception e) {
            return handleError(
                String.format("%s reviewId=%s, error=%s", CM041000MessageConstant.REVIEW_UPDATE_FAIL, reviewId, e.getMessage()),
                e,
                CM041000MessageConstant.REVIEW_UPDATE_FAIL
            );
        }
    }

    /**
     * 리뷰 에디터 이미지 DB 등록 (리스트)
     */
    @Transactional
    public ResponseEntity<ResponseModel<Integer>> insertEditorImages(Long reviewId, List<String> movedImagePaths, String loginId) {
        log.info("Start insertEditorImages: reviewId={}", reviewId);
        if (reviewId == null || movedImagePaths == null || movedImagePaths.isEmpty()) {
            log.warn("{} reviewId={}", CM041000MessageConstant.REVIEW_VALIDATION_FAIL, reviewId);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(0, false, CM041000MessageConstant.REVIEW_VALIDATION_FAIL));
        }
        String createdBy = resolveLoginId(loginId);
        int count = 0;
        try {
            for (String imagePath : movedImagePaths) {
                Attachments attachment = new Attachments();
                attachment.setReviewId(reviewId);
                attachment.setFilePath(imagePath);
                attachment.setFileName(Paths.get(imagePath).getFileName().toString());
                attachment.setFileType(getFileExtension(imagePath));
                attachment.setFileSize(0L);
                attachment.setCreatedBy(createdBy);
                attachment.setCreatedAt(LocalDateTime.now());
                cm041000Mapper.insertReviewAttachment(attachment);
                count++;
            }
            log.info("리뷰 에디터 이미지 DB 등록 완료 reviewId={}, count={}", reviewId, count);
            log.info("End insertEditorImages: reviewId={}", reviewId);
            return ResponseEntity
                    .ok(createResponseModel(count, true, CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_SUCCESS));
        } catch (DataAccessException dae) {
            return handleError(
                    String.format("%s reviewId=%s, error=%s", CM041000MessageConstant.REVIEW_DB_ERROR, reviewId,
                            dae.getMessage()),
                    dae,
                    CMMessageConstant.ERROR_DATABASE);
        } catch (Exception e) {
            return handleError(
                    String.format("%s reviewId=%s, error=%s", CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_FAIL,
                            reviewId, e.getMessage()),
                    e,
                    CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_FAIL);
        }
    }
    
    /**
     * 파일 경로에서 확장자 추출
     * 
     * @param filePath 파일 경로
     * @return 확장자 (점 제외), 없으면 빈 문자열
     */
    private String getFileExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }

        int lastDotIndex = filePath.lastIndexOf('.');
        int lastSlashIndex = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));

        if (lastDotIndex > lastSlashIndex && lastDotIndex < filePath.length() - 1) {
            return filePath.substring(lastDotIndex + 1).toLowerCase();
        }

        return "";
    }

    /**
     * 리뷰 입력값 검증 (reviewId, file)
     */
    private ResponseEntity<ResponseModel<String>> validateReviewInput(Long reviewId, MultipartFile file) {
        if (reviewId == null || reviewId <= 0) {
            log.warn("{} reviewId={}", CM041000MessageConstant.REVIEW_VALIDATION_FAIL, reviewId);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(null, false, CM041000MessageConstant.REVIEW_VALIDATION_FAIL));
        }
        if (file != null && file.isEmpty()) {
            log.warn("{} reviewId={}", CM041000MessageConstant.REVIEW_VALIDATION_FAIL, reviewId);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(null, false, CM041000MessageConstant.REVIEW_VALIDATION_FAIL));
        }
        return null;
    }

    /**
     * 리뷰 메인 이미지 저장 및 DB 업데이트
     */
    private ResponseEntity<ResponseModel<String>> saveMainImage(Long reviewId, MultipartFile file) {
        try {
            String reviewFolder = "review/" + reviewId + "/main";
            String savedName = saveFile(file, reviewFolder);
            String filePath = reviewFolder + "/" + savedName; // 파일 시스템 기준 상대 경로
            String imageUrl = "/images/" + filePath; // 정적 리소스 매핑 기준 URL
            cm041000Mapper.updateReviewImage(reviewId, imageUrl);
            log.info("{} reviewId={}, path={}, url={}", CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_SUCCESS, reviewId, filePath, imageUrl);
            return ResponseEntity.ok(createResponseModel(imageUrl, true, CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_SUCCESS));
        } catch (Exception e) {
            log.error("{} reviewId={}, error={}", CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_FAIL, reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(null, false, CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_FAIL));
        }
    }

    /**
     * 에디터 이미지 처리 및 DB 등록, ReviewRequest의 content 변경
     */
    private ResponseEntity<ResponseModel<Integer>> processAndRegisterEditorImages(Long reviewId, ReviewRequest review, String loginId) {
        if (review == null || review.getContent() == null) {
            return null;
        }
        List<String> movedEditorImages = new ArrayList<>();
        String originalContent = review.getContent();
        String processedContent = processEditorImages(originalContent, reviewId, movedEditorImages);
        review.setContent(processedContent);
        int attachCount = 0;
        if (!Objects.equals(processedContent, originalContent)) {
            try {
                cm041000Mapper.updateReviewContent(reviewId, processedContent, loginId);
                log.info("리뷰 에디터 콘텐츠 업데이트 완료 reviewId={}, movedImages={}", reviewId, movedEditorImages.size());
            } catch (Exception contentUpdateException) {
                log.error("리뷰 에디터 콘텐츠 업데이트 실패 reviewId={}, error={}",
                        reviewId, contentUpdateException.getMessage(), contentUpdateException);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createResponseModel(0, false, CM041000MessageConstant.REVIEW_WRITE_FAIL));
            }
        }
        if (!movedEditorImages.isEmpty()) {
            ResponseEntity<ResponseModel<Integer>> attachResp = insertEditorImages(reviewId, movedEditorImages, loginId);
            if (attachResp != null && attachResp.getBody() != null) {
                attachCount = attachResp.getBody().getResultList();
            }
        }
        return ResponseEntity.ok(createResponseModel(attachCount, true, CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_SUCCESS));
    }

    /**
     * 통합 에러 핸들링
     */
    private <T> ResponseEntity<ResponseModel<T>> handleError(String logMessage, Exception e, String responseMessage) {
        log.error(logMessage, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createResponseModel(null, false, responseMessage));
    }

    /**
     * 리뷰 에디터 이미지 삭제 (imageId 기반)
     */
    @Override
    @Transactional
    public boolean deleteReviewEditorImage(Long reviewId, Long imageId) {
        return deleteImageCommon("editor", reviewId, imageId);
    }

    /**
     * 리뷰 이미지 삭제 (imageId 기반)
     *
     * @param reviewId 리뷰 ID
     * @param imageId 리뷰 이미지 PK
     * @return 삭제 성공 여부
     */
    @Override
    @Transactional
    public boolean deleteReviewImage(Long reviewId, Long imageId) {
        return deleteImageCommon("main", reviewId, imageId);
    }

    /**
     * 공통 이미지 삭제 로직 (에디터/메인)
     *
     * @param type "editor" 또는 "main"
     * @param reviewId 리뷰 ID
     * @param imageId 이미지 PK (main의 경우 무시)
     * @return 삭제 성공 여부
     */
    private boolean deleteImageCommon(String type, Long reviewId, Long imageId) {
        try {
            if ("editor".equals(type)) {
                int affected = cm041000Mapper.deleteReviewAttachment(imageId, reviewId);
                if (affected > 0) {
                    log.info("리뷰 에디터 이미지 삭제 성공 reviewId={}, imageId={}", reviewId, imageId);
                    return true;
                } else {
                    log.warn("리뷰 에디터 이미지 삭제 실패 reviewId={}, imageId={}", reviewId, imageId);
                    return false;
                }
            } else if ("main".equals(type)) {
                cm041000Mapper.updateReviewImage(reviewId, null);
                log.info("{} reviewId={}", CM041000MessageConstant.REVIEW_IMAGE_DELETE_SUCCESS, reviewId);
                return true;
            } else {
                log.warn("알 수 없는 이미지 삭제 타입: {} reviewId={}, imageId={}", type, reviewId, imageId);
                return false;
            }
        } catch (Exception e) {
            if ("editor".equals(type)) {
                log.error("리뷰 에디터 이미지 삭제 오류 reviewId={}, imageId={}", reviewId, imageId, e);
            } else if ("main".equals(type)) {
                log.error("{} reviewId={}, imageId={}", CM041000MessageConstant.REVIEW_IMAGE_DELETE_FAIL, reviewId, imageId, e);
            } else {
                log.error("알 수 없는 이미지 삭제 타입 오류: {} reviewId={}, imageId={}", type, reviewId, imageId, e);
            }
            throw e;
        }
    }

    /**
     * 파일 저장
     * 
     * @param file 파일 정보
     * @return 저장된 파일명
     * @throws IOException       파일 저장 중 오류 발생 시
     * @throws SecurityException 보안 검증 실패 시
     */
    private String saveFile(MultipartFile file, String path) throws IOException, SecurityException {
        if (!isValidImageFile(file)) {
            throw new SecurityException("Invalid image file type");
        }

        if ("s3".equals(uploadType) && s3Service != null) {
            try {
                String tmpUrl = "images/".concat(path);
                String fileUrl = s3Service.uploadFile(file, tmpUrl);
                log.debug("S3 업로드 완료: {}", fileUrl);
                String fileName = Paths.get(fileUrl).getFileName().toString();
                return fileName;
            } catch (IOException e) {
                log.error("S3 업로드 실패", e);
                throw new IOException(CMMessageConstant.ERROR_FILE_SAVE_FAILED, e);
            }
        }

        Path uploadPath = Paths.get(uploadDir, path);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        }

        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = generateSecureRandomString(8);
        String savedFileName = String.format("%s_%s%s", timeStamp, uuid, fileExtension);

        Path filePath = uploadPath.resolve(savedFileName);
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return savedFileName;
        } catch (IOException e) {
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException deleteException) {
                // ignore cleanup error
            }
            throw new IOException("File save failed", e);
        }
    }

    /**
     * 보안을 위한 랜덤 문자열 생성
     * 
     * @param length 생성할 문자열 길이
     * @return 랜덤 문자열
     */
    private String generateSecureRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        String chars = uuidDir;
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 이미지 파일 형식 검증
     * 
     * @param file 파일
     * @return 유효한 이미지 파일 여부
     */
    private boolean isValidImageFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            return false;
        }
        String lowerCaseFileName = originalFileName.toLowerCase();
        return lowerCaseFileName.endsWith(".jpg")
            || lowerCaseFileName.endsWith(".jpeg")
            || lowerCaseFileName.endsWith(".png")
            || lowerCaseFileName.endsWith(".gif");
    }


    /**
     * 에디터 본문 내 임시 이미지 경로를 리뷰용 경로로 변환
     *
     * @param htmlContent 원본 HTML
     * @param reviewId    리뷰 ID
     * @param movedImagePaths 이동 성공한 이미지 경로 목록
     * @return 변환된 HTML
     */
    private String processEditorImages(String htmlContent, Long reviewId, List<String> movedImagePaths) {
        if (htmlContent == null || htmlContent.isBlank() || reviewId == null || reviewId <= 0) {
            return htmlContent;
        }

        Pattern pattern = Pattern.compile("<img[^>]+src=[\"']([^\"']*/tmp/[^\"']+)[\"'][^>]*>",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(htmlContent);

        String reviewFolder = "review/" + reviewId + "/editor";
        StringBuffer processedContent = new StringBuffer();
        boolean changed = false;

        while (matcher.find()) {
            String fullImgTag = matcher.group(0);
            String tmpImageUrl = matcher.group(1);
            String fileName = extractFileNameFromUrl(tmpImageUrl);

            if (fileName != null) {
                String newImagePath = moveTmpImageToReview(fileName, reviewFolder);
                if (newImagePath != null) {
                    String newImageUrl = buildEditorImageUrl(tmpImageUrl, newImagePath);
                    String replacedTag = fullImgTag.replace(tmpImageUrl, newImageUrl);
                    matcher.appendReplacement(processedContent, Matcher.quoteReplacement(replacedTag));

                    if (movedImagePaths != null) {
                        movedImagePaths.add(newImagePath);
                    }
                    changed = true;
                    continue;
                }
            }

            matcher.appendReplacement(processedContent, Matcher.quoteReplacement(fullImgTag));
        }

        matcher.appendTail(processedContent);
        return changed ? processedContent.toString() : htmlContent;
    }

    /**
     * 임시 폴더의 이미지를 리뷰 폴더로 이동
     *
     * @param fileName     파일명
     * @param reviewFolder 리뷰 폴더 (예: review/{id}/editor)
     * @return 이동된 이미지의 상대 경로, 실패 시 null
     */
    private String moveTmpImageToReview(String fileName, String reviewFolder) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }

        Path sourcePath = Paths.get(uploadDir, "tmp", fileName);
        Path targetDir = Paths.get(uploadDir, reviewFolder);
        Path targetPath = targetDir.resolve(fileName);

        try {
            if (!Files.exists(sourcePath)) {
                log.warn("임시 이미지가 존재하지 않아 이동을 건너뜁니다. fileName={}", fileName);
                return null;
            }

            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return reviewFolder + "/" + fileName;
        } catch (IOException e) {
            log.error("리뷰 임시 이미지 이동 실패 fileName={}, target={}", fileName, targetDir, e);
            return null;
        }
    }

    /**
     * 에디터 이미지 최종 URL 생성
     *
     * @param originalUrl 업로드 직후 임시 URL
     * @param newImagePath 리뷰 폴더 내 이동된 이미지 경로 (예: review/{id}/editor/file.jpg)
     * @return 정적 리소스 매핑이 가능한 최종 URL
     */
    private String buildEditorImageUrl(String originalUrl, String newImagePath) {
        String normalizedPath = newImagePath.startsWith("/") ? newImagePath.substring(1) : newImagePath;
        String reviewImagePath = "/images/" + normalizedPath;

        if (originalUrl == null || originalUrl.isBlank()) {
            return reviewImagePath;
        }

        int imagesTmpIndex = originalUrl.indexOf("/images/tmp/");
        if (imagesTmpIndex != -1) {
            String prefix = originalUrl.substring(0, imagesTmpIndex);
            return joinUrl(prefix, reviewImagePath);
        }

        int tmpIndex = originalUrl.indexOf("/tmp/");
        if (tmpIndex != -1) {
            String prefix = originalUrl.substring(0, tmpIndex);
            return joinUrl(prefix, reviewImagePath);
        }

        int imagesIndex = originalUrl.indexOf("/images/");
        if (imagesIndex != -1) {
            String prefix = originalUrl.substring(0, imagesIndex);
            return joinUrl(prefix, reviewImagePath);
        }

        return reviewImagePath;
    }

    /**
     * URL prefix와 경로를 안전하게 결합
     *
     * @param prefix 기존 URL prefix
     * @param path   덧붙일 경로
     * @return 결합된 URL
     */
    private String joinUrl(String prefix, String path) {
        if (prefix == null || prefix.isBlank()) {
            return path;
        }

        boolean prefixEndsWithSlash = prefix.endsWith("/");
        boolean pathStartsWithSlash = path.startsWith("/");

        if (prefixEndsWithSlash && pathStartsWithSlash) {
            return prefix + path.substring(1);
        }
        if (!prefixEndsWithSlash && !pathStartsWithSlash) {
            return prefix + "/" + path;
        }
        return prefix + path;
    }

    /**
     * 오래된 임시 이미지 삭제 (1시간 이상 경과 파일)
     */
    private void cleanupOldTmpImages() {
        Path tmpDir = Paths.get(uploadDir, "tmp");
        long threshold = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1);

        if (!Files.exists(tmpDir)) {
            return;
        }

        try {
            Files.list(tmpDir)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toMillis() < threshold;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        log.debug("임시 이미지 삭제 실패 path={}", path, e);
                    }
                });
        } catch (IOException e) {
            log.debug("임시 이미지 정리 중 오류 발생", e);
        }
    }

    /**
     * 이미지 URL에서 파일명 추출
     *
     * @param imageUrl 이미지 URL
     * @return 파일명 또는 null
     */
    private String extractFileNameFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        int tmpIndex = imageUrl.indexOf("/tmp/");
        if (tmpIndex != -1) {
            return imageUrl.substring(tmpIndex + 5);
        }

        int lastSlashIndex = imageUrl.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < imageUrl.length() - 1) {
            return imageUrl.substring(lastSlashIndex + 1);
        }

        return null;
    }

    /**
     * ResponseModel 생성
     * 
     * @param resultList 결과 데이터
     * @param result     처리 결과
     * @param message    메시지
     * @return ResponseModel
     */
    private <T> ResponseModel<T> createResponseModel(T resultList, boolean result, String message) {
        return ResponseModel.<T>builder()
                .resultList(resultList)
                .result(result)
                .message(Objects.requireNonNull(message, CMMessageConstant.MESSAGE_NULL_NOT_ALLOWED))
                .build();
    }

    /**
     * 세션에 담긴 로그인 ID를 우선 사용하고, 없으면 전달된 값이나 기본값으로 대체
     */
    private String resolveLoginId(String fallbackLoginId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String name = authentication.getName();
                if (name != null && !name.isBlank() && !"anonymousUser".equals(name)) {
                    return name;
                }
            }
        } catch (Exception ex) {
            log.debug("로그인 정보 조회 실패: {}", ex.getMessage());
        }
        if (fallbackLoginId != null && !fallbackLoginId.isBlank()) {
            return fallbackLoginId;
        }
        return "system";
    }
}
