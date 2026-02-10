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

    private final CM041001Mapper cm041001Mapper;

    /**
     * 指定レビューのコメント/返信コメントツリー取得
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReviewCommentInfo> getCommentsByReviewId(Long reviewId) {
        try {
            List<ReviewCommentInfo> flat = cm041001Mapper.selectCommentsByReviewId(reviewId);
            if (flat == null || flat.isEmpty()) {
                log.info(CM041001MessageConstant.COMMENT_TREE_EMPTY);
                return new ArrayList<>();
            }

            // フラットリストをツリー構造へ変換
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
                    else roots.add(c);
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
     * コメント／返信コメント登録（parentCommentIdがnullの場合は通常コメント、それ以外は返信コメント）
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
            if (cm041001Mapper.existsComment(parentCommentId) == 0) {
                log.warn(CM041001MessageConstant.COMMENT_PARENT_NOT_FOUND);
                return -2L;
            }
            Long parentOwnerId = cm041001Mapper.findCommentOwnerId(parentCommentId);
            if (parentOwnerId != null && parentOwnerId.equals(request.getUserId())) {
                log.warn(CM041001MessageConstant.COMMENT_SELF_REPLY_FORBIDDEN);
                return -3L;
            }
        }
        try {
            cm041001Mapper.insertComment(request);
            if (parentCommentId == null && request.getCommentId() != null) {
                cm041001Mapper.updateRootGroupId(request.getCommentId());
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
     * コメント更新（作成者本人のみ）
     */
    @Override
    @Transactional
    public boolean updateComment(Long reviewCommentId, Long userId, String content) {
        if (userId == null) {
            log.warn(CM041001MessageConstant.AUTH_REQUIRED);
            return false;
        }
        try {
            int rows = cm041001Mapper.updateComment(reviewCommentId, userId, content);
            return rows > 0;
        } catch (Exception e) {
            log.error(CM041001MessageConstant.COMMENT_DB_ERROR, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * コメント削除（論理削除、作成者本人のみ）
     */
    @Override
    @Transactional
    public boolean softDeleteComment(Long reviewId, Long reviewCommentId, Long userId) {
        if (userId == null) {
            log.warn(CM041001MessageConstant.AUTH_REQUIRED);
            return false;
        }
        log.info(CM041001MessageConstant.COMMENT_SOFT_DELETE_CALLED, reviewId, reviewCommentId, userId);
        try {
            int rows = cm041001Mapper.softDeleteComment(reviewId, reviewCommentId, userId);
            return rows > 0;
        } catch (Exception e) {
            log.error(CM041001MessageConstant.COMMENT_DB_ERROR, e.getMessage(), e);
            return false;
        }
    }
}
