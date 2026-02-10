package com.cloudia.backend.CM_04_1000.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
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
import com.cloudia.backend.CM_04_1000.model.ReviewInfo;
import com.cloudia.backend.CM_04_1000.model.ReviewRequest;
import com.cloudia.backend.CM_04_1000.service.CM041000Service;
import com.cloudia.backend.config.jwt.JwtTokenProvider;
import com.cloudia.backend.common.model.ResponseModel;

import org.springframework.util.DigestUtils;

import lombok.RequiredArgsConstructor; 
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/guest")
public class CM041000Controller {
    private final CM041000Service cm041000Service;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * レビュー一覧を取得
     *
     * @return レビュー一覧
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
     * レビュー登録（画像あり）
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
     * レビュー更新（画像あり）
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
     * レビュー本文エディタ画像アップロード
     */
    @PostMapping("/reviews/image/upload")
    public ResponseEntity<ResponseModel<String>> uploadEditorImage(@RequestParam("file") MultipartFile file) {
        return cm041000Service.uploadReviewEditorImage(null, file);
    }

    /**
     * レビュー詳細を取得
     *
     * @param reviewId レビューID
     * @return レビュー（単件）
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
     * レビュー閲覧数を増加（1日1回制限）
     *
     * @param reviewId レビューID
     * @return 処理結果
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
     * 投稿者向け：注文＋商品一覧を取得
     *
     * @param memberNumber 会員番号
     * @return 注文＋商品一覧
     */
    @GetMapping("/reviews/orders")
    public ResponseEntity<ResponseModel<List<OrderDetailResponse>>> getOrdersWithProducts(
            @RequestParam String memberNumber) {
        try {
            List<OrderDetailResponse> list = cm041000Service.findOrdersWithProducts(memberNumber);
            return ResponseEntity.ok(
                setResponseDto(list, true, CM041000MessageConstant.REVIEW_ORDER_FETCH_SUCCESS));
        } catch (Exception e) {
            log.error("注文＋商品一覧の取得に失敗: memberNumber={}", memberNumber, e);
            return ResponseEntity.internalServerError()
                .body(setResponseDto(null, false, CM041000MessageConstant.REVIEW_ORDER_FETCH_FAIL));
        }
    }
    
    /**
     * レビューをソフトデリート（本人のみ可能）
     *
     * @param reviewId レビューID
     * @return ソフトデリート結果
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
     * レビュー画像を削除（本人のみ可能、imageId基準）
     *
     * @param reviewId レビューID
     * @param imageId レビュー画像PK
     * @return 削除結果
     */
    @DeleteMapping("/reviews/{reviewId}/images/{imageId}")
    public ResponseEntity<ResponseModel<Void>> deleteReviewImage(
            @PathVariable Long reviewId,
            @PathVariable Long imageId) {
        try {
            boolean deleted = cm041000Service.deleteReviewImage(reviewId, imageId);
            if (!deleted) {
                log.warn("レビュー画像の削除に失敗 reviewId={}, imageId={}", reviewId, imageId);
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
     * レビューのメイン画像をアップロード
     *
     * @param reviewId レビューID
     * @param file アップロードする画像
     * @return アップロード成功時は画像URL、失敗時はnull
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
     * JWTトークンからユーザーIDを抽出
     *
     * @param request HTTPリクエスト
     * @return ユーザーID（トークンが無い／無効な場合はnull）
     */
    private Long extractUserId(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token == null) {
            return null;
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        if (userId == null) {
            log.debug("JWTトークンからuserIdの抽出に失敗しました");
        }
        return userId;
    }

    /**
     * ビューアキーを生成。
     * ログインユーザーは "user:{userId}"、
     * 未ログインユーザーはクライアントIPのMD5ハッシュを用いた "guest:{hashedIp}" 形式。
     *
     * @param userId  ユーザーID（ログイン時）
     * @param request HTTPリクエスト
     * @return ビューアキー
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
     * クライアントの実IPアドレスを取得。
     * "X-Forwarded-For" ヘッダがあれば先頭のIPを使用し、
     * なければリモートアドレスを返す。
     *
     * @param request HTTPリクエスト
     * @return クライアントIP
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 共通レスポンスフォーマット設定
     *
     * @param resultList 結果データ
     * @param ret        成功可否
     * @param msg        メッセージ
     * @return 共通レスポンスモデル
     */
    private <T> ResponseModel<T> setResponseDto(T resultList, boolean ret, String msg) {
        return ResponseModel.<T>builder()
            .resultList(resultList)
            .result(ret)
            .message(msg)
            .build();
    }
}
