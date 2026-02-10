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
import com.cloudia.backend.common.model.ResponseModel;
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
     * レビュー削除
     *
     * @param reviewId 削除するレビューID
     * @return 削除結果
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

            // 関連コメント／返信コメントの論理削除
            try {
                cm041001Mapper.deleteCommentsByReviewId(reviewId);
            } catch (Exception e) {
                log.warn("レビュー削除時のコメント削除に失敗しました reviewId={}, error={}", reviewId, e.getMessage(), e);
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
     * 特定商品のレビュー一覧取得（任意ページネーション）
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
     * レビュー単体取得
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
     * レビュー全件取得（productId 指定なし）
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReviewInfo> findAllReviews() {
        return findReviewsByProduct(null, null, null);
    }



    /**
     * レビュー閲覧数を「1日1回」だけ増加させる処理
     *
     * @param reviewId レビューID
     * @param userId   ユーザーID
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
     * 注文＋商品一覧取得
     *
     * @param memberNumber 会員番号
     * @return 注文詳細リスト
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDetailResponse> findOrdersWithProducts(String memberNumber) {
        return cm041000Mapper.selectOrdersWithProducts(memberNumber);
    }

    /**
     * 注文内に該当商品が存在するか検証
     *
     * @param memberNumber 会員番号
     * @param orderNumber  注文番号
     * @param productCode  商品コード
     * @return true: 存在する / false: 存在しない
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
     * レビューのメイン画像アップロード
     *
     * @param reviewId レビューID
     * @param file アップロード画像
     * @return アップロード済み画像URL
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
     * レビュー本文エディタ画像の一時アップロード（作成/更新時に移動）
     *
     * @param reviewId レビューID（null または 0 の場合は一時アップロード）
     * @param file アップロード画像
     * @return アップロード済み画像URL
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
                log.debug("一時画像の整理中に警告 reviewId={}, error={}", reviewId, cleanupEx.getMessage());
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
     * レビュー作成＋メイン/エディタ画像処理
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
            // バリデーションおよび商品／注文チェック
            Integer reviewType = review != null ? review.getReviewType() : null;
            boolean isReviewType = Objects.equals(reviewType, 0); // 0=レビュー、1=口コミ（購入後の感想）
            boolean requireProduct = !isReviewType; // 口コミの場合のみ商品が必須
            boolean hasProductCode = StringUtils.hasText(review.getProductCode());

            if (requireProduct && !hasProductCode) {
                log.warn("{} 商品コードがありません。memberNumber={}, orderNumber={}, productCode={}",
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
                review.setProductCode("");
                if (!StringUtils.hasText(review.getOrderNumber())) {
                    review.setOrderNumber("");
                }
            } else {
                log.info("注文番号なしのオプションレビュー経路 - 注文検証をスキップ memberNumber={}, orderNumber={}",
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
            // メイン画像保存およびパス更新
            if (file != null && !file.isEmpty()) {
                ResponseEntity<ResponseModel<String>> mainImgResp = saveMainImage(reviewId, file);
                if (mainImgResp.getBody() == null || !mainImgResp.getBody().isResult()) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createResponseModel(null, false, CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_FAIL));
                }
            }
            // エディタ画像処理およびDB登録
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
     * レビュー更新＋メイン/エディタ画像処理
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
            // メイン画像保存およびパス更新
            if (file != null && !file.isEmpty()) {
                ResponseEntity<ResponseModel<String>> mainImgResp = saveMainImage(reviewId, file);
                if (mainImgResp.getBody() == null || !mainImgResp.getBody().isResult()) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createResponseModel(false, false, CM041000MessageConstant.REVIEW_IMAGE_UPLOAD_FAIL));
                }
            }
            // エディタ画像処理およびDB登録
            ResponseEntity<ResponseModel<Integer>> editorResp = processAndRegisterEditorImages(reviewId, req, loginId);
            if (editorResp != null && (editorResp.getBody() == null || !editorResp.getBody().isResult())) {
                log.warn("エディタ画像処理に失敗しました reviewId={}", reviewId);
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
     * レビューエディタ画像のDB登録（リスト）
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
            log.info("レビューエディタ画像のDB登録完了 reviewId={}, count={}", reviewId, count);
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
     * ファイルパスから拡張子を抽出
     *
     * @param filePath ファイルパス
     * @return 拡張子（ドット除外）、なければ空文字
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
     * レビュー入力値検証（reviewId, file）
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
     * レビューのメイン画像保存およびDB更新
     */
    private ResponseEntity<ResponseModel<String>> saveMainImage(Long reviewId, MultipartFile file) {
        try {
            String reviewFolder = "review/" + reviewId + "/main";
            String savedName = saveFile(file, reviewFolder);
            String filePath = reviewFolder + "/" + savedName; // ファイルシステム基準の相対パス
            String imageUrl = "/images/" + filePath; // 静的リソースマッピング基準のURL
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
     * エディタ画像処理およびDB登録、ReviewRequest の content を更新
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
                log.info("レビューエディタ本文の更新完了 reviewId={}, movedImages={}", reviewId, movedEditorImages.size());
            } catch (Exception contentUpdateException) {
                log.error("レビューエディタ本文の更新に失敗しました reviewId={}, error={}",
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
     * 共通エラーハンドリング
     */
    private <T> ResponseEntity<ResponseModel<T>> handleError(String logMessage, Exception e, String responseMessage) {
        log.error(logMessage, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createResponseModel(null, false, responseMessage));
    }

    /**
     * レビューエディタ画像削除（imageId ベース）
     */
    @Override
    @Transactional
    public boolean deleteReviewEditorImage(Long reviewId, Long imageId) {
        return deleteImageCommon("editor", reviewId, imageId);
    }

    /**
     * レビュー画像削除（imageId ベース）
     *
     * @param reviewId レビューID
     * @param imageId レビュー画像PK
     * @return 削除成功可否
     */
    @Override
    @Transactional
    public boolean deleteReviewImage(Long reviewId, Long imageId) {
        return deleteImageCommon("main", reviewId, imageId);
    }

    /**
     * 共通画像削除ロジック（エディタ/メイン）
     *
     * @param type "editor" または "main"
     * @param reviewId レビューID
     * @param imageId 画像PK（main の場合は無視）
     * @return 削除成功可否
     */
    private boolean deleteImageCommon(String type, Long reviewId, Long imageId) {
        try {
            if ("editor".equals(type)) {
                int affected = cm041000Mapper.deleteReviewAttachment(imageId, reviewId);
                if (affected > 0) {
                    log.info("レビューエディタ画像の削除に成功 reviewId={}, imageId={}", reviewId, imageId);
                    return true;
                } else {
                    log.warn("レビューエディタ画像の削除に失敗 reviewId={}, imageId={}", reviewId, imageId);
                    return false;
                }
            } else if ("main".equals(type)) {
                cm041000Mapper.updateReviewImage(reviewId, null);
                log.info("{} reviewId={}", CM041000MessageConstant.REVIEW_IMAGE_DELETE_SUCCESS, reviewId);
                return true;
            } else {
                log.warn("不明な画像削除タイプ: {} reviewId={}, imageId={}", type, reviewId, imageId);
                return false;
            }
        } catch (Exception e) {
            if ("editor".equals(type)) {
                log.error("리뷰 에디터 이미지 삭제 오류 reviewId={}, imageId={}", reviewId, imageId, e);
            } else if ("main".equals(type)) {
                log.error("{} reviewId={}, imageId={}", CM041000MessageConstant.REVIEW_IMAGE_DELETE_FAIL, reviewId, imageId, e);
            } else {
                log.error("不明な画像削除タイプのエラー: {} reviewId={}, imageId={}", type, reviewId, imageId, e);
            }
            throw e;
        }
    }

    /**
     * ファイル保存
     *
     * @param file ファイル情報
     * @return 保存されたファイル名
     * @throws IOException       ファイル保存中にエラーが発生した場合
     * @throws SecurityException セキュリティ検証に失敗した場合
     */
    private String saveFile(MultipartFile file, String path) throws IOException, SecurityException {
        if (!isValidImageFile(file)) {
            throw new SecurityException("Invalid image file type");
        }

        if ("s3".equals(uploadType) && s3Service != null) {
            try {
                String tmpUrl = "images/".concat(path);
                String fileUrl = s3Service.uploadFile(file, tmpUrl);
                log.debug("S3アップロード完了: {}", fileUrl);
                String fileName = Paths.get(fileUrl).getFileName().toString();
                return fileName;
            } catch (IOException e) {
                log.error("S3 アップロード失敗", e);
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
            throw new IOException("ファイル保存に失敗しました", e);
        }
    }

    /**
     * セキュリティ向けランダム文字列生成
     *
     * @param length 生成する文字列長
     * @return ランダム文字列
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
     * 画像ファイル形式検証
     *
     * @param file ファイル
     * @return 有効な画像ファイルかどうか
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
     * エディタ本文内の一時画像パスをレビュー用パスへ変換
     *
     * @param htmlContent 元のHTML
     * @param reviewId    レビューID
     * @param movedImagePaths 移動成功した画像パス一覧
     * @return 変換後HTML
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
     * 一時フォルダの画像をレビュー用フォルダへ移動
     *
     * @param fileName     ファイル名
     * @param reviewFolder レビューフォルダ（例: review/{id}/editor）
     * @return 移動後画像の相対パス（失敗時は null）
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
                log.warn("一時画像が存在しないため移動をスキップします。fileName={}", fileName);
                return null;
            }

            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return reviewFolder + "/" + fileName;
        } catch (IOException e) {
            log.error("レビュー一時画像の移動に失敗しました fileName={}, target={}", fileName, targetDir, e);
            return null;
        }
    }

    /**
     * エディタ画像の最終URL生成
     *
     * @param originalUrl アップロード直後の一時URL
     * @param newImagePath レビューフォルダ内へ移動した画像パス（例: review/{id}/editor/file.jpg）
     * @return 静的リソースとして参照可能な最終URL
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
     * URL の prefix とパスを安全に結合
     *
     * @param prefix 既存URLのprefix
     * @param path   追加するパス
     * @return 結合後URL
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
     * 古い一時画像を削除（1時間以上経過したファイル）
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
                        log.debug("一時画像の削除に失敗 path={}", path, e);
                    }
                });
        } catch (IOException e) {
            log.debug("一時画像の整理中にエラーが発生しました", e);
        }
    }

    /**
     * 画像URLからファイル名を抽出
     *
     * @param imageUrl 画像URL
     * @return ファイル名、または null
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
     * ResponseModel 生成
     *
     * @param resultList 結果データ
     * @param result     処理結果
     * @param message    メッセージ
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
     * セッション内のログインIDを優先し、なければ引数またはデフォルト値にフォールバック
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
            log.debug("ログイン情報の取得に失敗: {}", ex.getMessage());
        }
        if (fallbackLoginId != null && !fallbackLoginId.isBlank()) {
            return fallbackLoginId;
        }
        return "system";
    }
}
