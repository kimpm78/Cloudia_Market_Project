package com.cloudia.backend.CM_04_1001.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_04_1001.constants.CM041001MessageConstant;
import com.cloudia.backend.CM_04_1001.mapper.CM041001Mapper;
import com.cloudia.backend.CM_04_1001.model.ReviewCommentInfo;
import com.cloudia.backend.CM_04_1001.model.ReviewCommentRequest;
import com.cloudia.backend.CM_04_1001.service.CM041001Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM041001ServiceImpl implements CM041001Service {

    private final CM041001Mapper mapper;

    /**
     * 특정 리뷰의 댓글/대댓글 트리 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReviewCommentInfo> getCommentsByReviewId(Long reviewId) {
        try {
            List<ReviewCommentInfo> flat = mapper.selectCommentsByReviewId(reviewId);
            if (flat == null || flat.isEmpty()) {
                log.info(CM041001MessageConstant.COMMENT_TREE_EMPTY);
                return new ArrayList<>();
            }

            // 평탄 리스트 → 트리 변환
            Map<Long, ReviewCommentInfo> byId = new HashMap<>();
            for (ReviewCommentInfo c : flat) {
                byId.put(c.getCommentId(), c);
                if (c.getChildren() == null) c.setChildren(new ArrayList<>());
            }
            List<ReviewCommentInfo> roots = new ArrayList<>();
            for (ReviewCommentInfo c : flat) {
                Long parentId = c.getParentCommentId();
                if (parentId == null) {
                    roots.add(c);
                } else {
                    ReviewCommentInfo parent = byId.get(parentId);
                    if (parent != null) parent.getChildren().add(c);
                    else roots.add(c); // 고아 방지
                }
            }
            log.info(CM041001MessageConstant.COMMENT_TREE_FETCH_SUCCESS);
            return roots;
        } catch (Exception e) {
            log.error(CM041001MessageConstant.COMMENT_DB_ERROR, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 댓글 및 대댓글 등록 (parentCommentId가 null이면 일반 댓글, 아니면 대댓글)
     */
    @Override
    @Transactional
    public Long saveComment(ReviewCommentRequest request) {
        if (request == null || request.getUserId() == null) {
            log.warn(CM041001MessageConstant.AUTH_REQUIRED);
            return null;
        }
        Long parentCommentId = request.getParentCommentId();
        if (parentCommentId != null) {
            if (mapper.existsComment(parentCommentId) == 0) {
                log.warn(CM041001MessageConstant.COMMENT_PARENT_NOT_FOUND);
                return -2L;
            }
            Long parentOwnerId = mapper.findCommentOwnerId(parentCommentId);
            if (parentOwnerId != null && parentOwnerId.equals(request.getUserId())) {
                log.warn(CM041001MessageConstant.COMMENT_SELF_REPLY_FORBIDDEN);
                return -3L;
            }
        }
        try {
            mapper.insertComment(request);
            if (parentCommentId == null && request.getCommentId() != null) {
                mapper.updateRootGroupId(request.getCommentId());
            }
            if (parentCommentId == null) {
                log.info(CM041001MessageConstant.COMMENT_CREATE_SUCCESS);
            } else {
                log.info(CM041001MessageConstant.REPLY_CREATE_SUCCESS);
            }
            return request.getCommentId();
        } catch (Exception e) {
            log.error(CM041001MessageConstant.COMMENT_DB_ERROR, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 댓글 수정 (작성자 본인만)
     */
    @Override
    @Transactional
    public boolean updateComment(Long reviewCommentId, Long userId, String content) {
        if (userId == null) {
            log.warn(CM041001MessageConstant.AUTH_REQUIRED);
            return false;
        }
        try {
            int rows = mapper.updateComment(reviewCommentId, userId, content); // WHERE절에 user_id 조건은 XML에서 처리 권장
            return rows > 0;
        } catch (Exception e) {
            log.error(CM041001MessageConstant.COMMENT_DB_ERROR, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 댓글 삭제 (Soft Delete, 작성자 본인만)
     */
    @Override
    @Transactional
    public boolean softDeleteComment(Long reviewId, Long reviewCommentId, Long userId) {
        if (userId == null) {
            log.warn(CM041001MessageConstant.AUTH_REQUIRED);
            return false;
        }
        log.info("softDeleteComment called with reviewId={}, reviewCommentId={}, userId={}", reviewId, reviewCommentId, userId);
        try {
            int rows = mapper.softDeleteComment(reviewId, reviewCommentId, userId);
            return rows > 0;
        } catch (Exception e) {
            log.error(CM041001MessageConstant.COMMENT_DB_ERROR, e.getMessage(), e);
            return false;
        }
    }
}
