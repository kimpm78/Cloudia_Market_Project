package com.cloudia.backend.CM_04_1001.controller;

import java.util.stream.Collectors;
import java.util.List;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.cloudia.backend.CM_04_1001.constants.CM041001MessageConstant;
import com.cloudia.backend.CM_04_1001.service.CM041001Service;

import com.cloudia.backend.CM_04_1001.model.ReviewCommentRequest;
import com.cloudia.backend.CM_04_1001.model.ReviewCommentInfo;
import com.cloudia.backend.common.model.ResponseModel;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/guest")
public class CM041001Controller {

    private final CM041001Service cm041001Service;

    /**
     * 特定レビューのコメント一覧取得
     */
    @GetMapping("/reviews/{reviewId}/comments")
    public ResponseEntity<ResponseModel<List<ReviewCommentInfo>>> getReviewComments(@PathVariable Long reviewId) {
        List<ReviewCommentInfo> comments = cm041001Service.getCommentsByReviewId(reviewId);
        String message = comments == null || comments.isEmpty()
                ? CM041001MessageConstant.COMMENT_TREE_EMPTY
                : CM041001MessageConstant.COMMENT_TREE_FETCH_SUCCESS;
        return ResponseEntity.ok(setResponseDto(comments, true, message));
    }

    /**
     * レビューコメント登録
     */
    @PostMapping("/reviews/{reviewId}/comments")
    public ResponseEntity<ResponseModel<Long>> createReviewComment(@PathVariable Long reviewId,
            @RequestBody @Valid ReviewCommentRequest commentRequest,
            BindingResult bindingResult) {
        // バリデーションエラー処理ヘルパーを使用
        ResponseEntity<ResponseModel<Long>> errorResponse = handleValidationErrors(bindingResult);
        if (errorResponse != null) {
            return errorResponse;
        }
        commentRequest.setReviewId(reviewId);
        // サービスは作成されたコメントのID(Long)を返却する必要がある
        Long createdId = cm041001Service.saveComment(commentRequest);
        if (createdId != null && createdId == -3L) {
            return ResponseEntity.badRequest()
                .body(setResponseDto(null, false, CM041001MessageConstant.COMMENT_SELF_REPLY_FORBIDDEN));
        }
        if (createdId != null && createdId == -2L) {
            return ResponseEntity.badRequest()
                .body(setResponseDto(null, false, CM041001MessageConstant.COMMENT_PARENT_NOT_FOUND));
        }
        if (createdId == null || createdId <= 0) {
            return ResponseEntity.badRequest().body(setResponseDto(null, false, CM041001MessageConstant.COMMENT_CREATE_FAIL));
        }
        return ResponseEntity.ok(setResponseDto(createdId, true, CM041001MessageConstant.COMMENT_CREATE_SUCCESS));
    }

    /**
     * 返信コメント登録
     */
    @PostMapping("/reviews/{reviewId}/comments/{parentId}/replies")
    public ResponseEntity<ResponseModel<Long>> createReplyComment(@PathVariable Long reviewId,
        @PathVariable Long parentId,
        @RequestBody @Valid ReviewCommentRequest replyRequest,
        BindingResult bindingResult) {
        // バリデーションエラー処理ヘルパーを使用
        ResponseEntity<ResponseModel<Long>> errorResponse = handleValidationErrors(bindingResult);
        if (errorResponse != null) {
            return errorResponse;
        }
        replyRequest.setReviewId(reviewId);
        replyRequest.setParentCommentId(parentId);
        // サービスは作成された返信コメントのID(Long)を返却する必要がある
        Long createdId = cm041001Service.saveComment(replyRequest);
        if (createdId != null && createdId == -3L) {
            return ResponseEntity.badRequest()
                .body(setResponseDto(null, false, CM041001MessageConstant.COMMENT_SELF_REPLY_FORBIDDEN));
        }
        if (createdId != null && createdId == -2L) {
            return ResponseEntity.badRequest()
                .body(setResponseDto(null, false, CM041001MessageConstant.COMMENT_PARENT_NOT_FOUND));
        }
        if (createdId == null || createdId <= 0) {
            return ResponseEntity.badRequest()
                .body(setResponseDto(null, false, CM041001MessageConstant.REPLY_CREATE_FAIL));
        }
        return ResponseEntity.ok(setResponseDto(createdId, true, CM041001MessageConstant.REPLY_CREATE_SUCCESS));
    }
    /**
     * BindingResultのエラーを処理してResponseEntityを返すヘルパー
     */
    private ResponseEntity<ResponseModel<Long>> handleValidationErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining("\r\n "));
            log.warn(CM041001MessageConstant.COMMENT_VALIDATION_FAIL, errorMessage);
            return ResponseEntity.badRequest().body(setResponseDto(null, false, errorMessage));
        }
        return null;
    }

    /**
     * コメント更新（本人のみ）
     */
    @PutMapping("/reviews/{reviewId}/comments/{commentId}")
    public ResponseEntity<ResponseModel<Void>> updateReviewComment(
            @PathVariable Long reviewId,
            @PathVariable Long commentId,
            @RequestBody @Valid ReviewCommentRequest updateRequest,
            BindingResult bindingResult) {
        
        // バリデーション
        ResponseEntity<ResponseModel<Long>> errorResponse = handleValidationErrors(bindingResult);
        if (errorResponse != null) {
            return ResponseEntity.badRequest()
                    .body(setResponseDto(null, false, CM041001MessageConstant.COMMENT_UPDATE_FAIL));
        }

        updateRequest.setReviewId(reviewId);
        updateRequest.setCommentId(commentId);

        log.info("User {} attempting to update comment {} on review {} with content={}", 
                updateRequest.getUserId(), commentId, reviewId, updateRequest.getContent());

        boolean updated = cm041001Service.updateComment(
            updateRequest.getCommentId(),
            updateRequest.getUserId(),
            updateRequest.getContent()
        );

        if (!updated) {
            return ResponseEntity.badRequest()
                    .body(setResponseDto(null, false, CM041001MessageConstant.COMMENT_UPDATE_FAIL));
        }
        return ResponseEntity.ok(setResponseDto(null, true, CM041001MessageConstant.COMMENT_UPDATE_SUCCESS));
    }

    /**
     * コメント削除（本人のみ、ソフトデリート）
     */
    @DeleteMapping("/reviews/{reviewId}/comments/{commentId}")
    public ResponseEntity<ResponseModel<Void>> deleteReviewComment(
            @PathVariable Long reviewId,
            @PathVariable Long commentId,
            @RequestBody ReviewCommentRequest deleteRequest) {
        
        Long userId = deleteRequest.getUserId();
        if (userId == null) {
            log.warn("Attempt to delete comment without userId. reviewId={}, commentId={}", reviewId, commentId);
            return ResponseEntity.status(401)
                .body(setResponseDto(null, false, CM041001MessageConstant.COMMENT_DELETE_FAIL));
        }

        log.info("User {} attempting to delete comment {} on review {}", userId, commentId, reviewId);
        boolean deleted = cm041001Service.softDeleteComment(reviewId, commentId, userId);
        if (!deleted) {
            return ResponseEntity.badRequest()
                .body(setResponseDto(null, false, CM041001MessageConstant.COMMENT_DELETE_FAIL));
        }
        return ResponseEntity.ok(setResponseDto(null, true, CM041001MessageConstant.COMMENT_DELETE_SUCCESS));
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
