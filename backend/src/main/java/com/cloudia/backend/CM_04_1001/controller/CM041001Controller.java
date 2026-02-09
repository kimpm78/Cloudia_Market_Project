package com.cloudia.backend.CM_04_1001.controller;

import java.util.stream.Collectors;
import java.util.List;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import com.cloudia.backend.CM_04_1001.model.ResponseModel;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/guest")
@CrossOrigin(origins = "*")
public class CM041001Controller {

    private final CM041001Service cm041001Service;

    /**
     * 특정 리뷰에 대한 댓글 목록 조회
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
     * 리뷰 댓글 등록
     * 변경: 
     * - 유효성 검사 중복 코드 제거를 위해 handleValidationErrors() 헬퍼 메서드 사용
     * - 반환 타입을 ResponseEntity<ResponseModel<Long>>로 변경하여 생성된 댓글 ID 반환
     * - 서비스 호출에서 parentId 파라미터 제거 (서비스에서 변경됨을 가정)
     */
    @PostMapping("/reviews/{reviewId}/comments")
    public ResponseEntity<ResponseModel<Long>> createReviewComment(@PathVariable Long reviewId,
            @RequestBody @Valid ReviewCommentRequest commentRequest,
            BindingResult bindingResult) {
        // 유효성 검사 에러 처리 헬퍼 사용
        ResponseEntity<ResponseModel<Long>> errorResponse = handleValidationErrors(bindingResult);
        if (errorResponse != null) {
            return errorResponse;
        }
        commentRequest.setReviewId(reviewId);
        // 서비스는 생성된 댓글의 ID(Long)를 반환해야 함
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
     * 대댓글 등록
     * 변경:
     * - 유효성 검사 중복 코드 제거를 위해 handleValidationErrors() 헬퍼 메서드 사용
     * - 반환 타입을 ResponseEntity<ResponseModel<Long>>로 변경하여 생성된 대댓글 ID 반환
     * - 서비스 호출에서 parentId 파라미터 제거 (서비스에서 변경됨을 가정)
     */
    @PostMapping("/reviews/{reviewId}/comments/{parentId}/replies")
    public ResponseEntity<ResponseModel<Long>> createReplyComment(@PathVariable Long reviewId,
        @PathVariable Long parentId,
        @RequestBody @Valid ReviewCommentRequest replyRequest,
        BindingResult bindingResult) {
        // 유효성 검사 에러 처리 헬퍼 사용
        ResponseEntity<ResponseModel<Long>> errorResponse = handleValidationErrors(bindingResult);
        if (errorResponse != null) {
            return errorResponse;
        }
        replyRequest.setReviewId(reviewId);
        replyRequest.setParentCommentId(parentId);
        // 서비스는 생성된 대댓글의 ID(Long)를 반환해야 함
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
     * BindingResult 에러를 처리하여 ResponseEntity 반환하는 헬퍼 메서드
     * (유효성 검사 코드 중복 제거)
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
     * 댓글 수정 (본인만 가능)
     */
    @PutMapping("/reviews/{reviewId}/comments/{commentId}")
    public ResponseEntity<ResponseModel<Void>> updateReviewComment(
            @PathVariable Long reviewId,
            @PathVariable Long commentId,
            @RequestBody @Valid ReviewCommentRequest updateRequest,
            BindingResult bindingResult) {
        
        // 유효성 검사
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
     * 댓글 삭제 (본인만 가능, 소프트 딜리트)
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
