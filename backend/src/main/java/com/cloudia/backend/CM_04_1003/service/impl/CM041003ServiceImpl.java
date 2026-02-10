package com.cloudia.backend.CM_04_1003.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.cloudia.backend.CM_04_1003.constants.CM041003Constant;
import com.cloudia.backend.CM_04_1003.constants.CM041003MessageConstant;
import com.cloudia.backend.CM_04_1003.mapper.CM041003Mapper;
import com.cloudia.backend.CM_04_1003.model.QnaAnswerRequest;
import com.cloudia.backend.CM_04_1003.model.QnaCreateRequest;
import com.cloudia.backend.CM_04_1003.model.QnaCreateResponse;
import com.cloudia.backend.CM_04_1003.model.QnaDetail;
import com.cloudia.backend.CM_04_1003.model.QnaDetailResponse;
import com.cloudia.backend.CM_04_1003.model.QnaListResponse;
import com.cloudia.backend.CM_04_1003.model.QnaSummary;
import com.cloudia.backend.CM_04_1003.service.CM041003Service;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.constants.CMMessageConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM041003ServiceImpl implements CM041003Service {

    private final CM041003Mapper cm041003Mapper;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<QnaListResponse>> getQnaList(int page, int size, String searchKeyword,
            Integer searchType) {
        if (page < 1 || size < 1) {
            log.warn(CM041003MessageConstant.LOG_INVALID_PAGING, page, size);
            return ResponseEntity.badRequest()
                    .body(buildResponse(null, false, CM041003MessageConstant.INVALID_PAGING));
        }

        final int safePage = Math.max(page, CM041003Constant.DEFAULT_PAGE);
        final int safeSize = Math.min(Math.max(size, 1), CM041003Constant.MAX_PAGE_SIZE);
        final int offset = (safePage - 1) * safeSize;
        final int effectiveSearchType = (searchType == null || searchType < 1 || searchType > 4) ? 1 : searchType;
        final String keyword = StringUtils.hasText(searchKeyword) ? searchKeyword.trim() : null;

        Map<String, Object> params = new HashMap<>();
        params.put("offset", offset);
        params.put("size", safeSize);
        params.put("searchType", effectiveSearchType);
        if (keyword != null) {
            params.put("keyword", keyword);
        }

        try {
            long total = cm041003Mapper.countQnaList(params);
            List<QnaSummary> items = total > 0 ? cm041003Mapper.selectQnaList(params) : Collections.emptyList();
            items = Objects.requireNonNullElse(items, Collections.emptyList());

            QnaListResponse listResponse = new QnaListResponse();
            listResponse.setItems(items);
            listResponse.setTotalCount(total);
            listResponse.setPage(safePage);
            listResponse.setSize(safeSize);

            return ResponseEntity.ok(buildResponse(listResponse, true, CM041003MessageConstant.QNA_LIST_SUCCESS));
        } catch (DataAccessException dae) {
            log.error(CM041003MessageConstant.LOG_QNA_LIST_DB_ERROR, dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(null, false, CMMessageConstant.ERROR_DATABASE));
        } catch (Exception e) {
            log.error(CM041003MessageConstant.LOG_QNA_LIST_UNEXPECTED_ERROR, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(null, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<QnaDetailResponse>> getQnaDetail(Long qnaId, Long requesterId, boolean admin) {
        if (qnaId == null || qnaId <= 0) {
            log.warn(CM041003MessageConstant.LOG_INVALID_QNA_ID_REQUEST, qnaId);
            return ResponseEntity.badRequest()
                    .body(buildResponse(null, false, CM041003MessageConstant.INVALID_REQUEST));
        }

        try {
            QnaDetail current = cm041003Mapper.selectQnaDetail(qnaId);
            if (current == null) {
                log.warn(CM041003MessageConstant.LOG_QNA_NOT_FOUND, qnaId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(buildResponse(null, false, CM041003MessageConstant.QNA_NOT_FOUND));
            }

            boolean isPrivate = current.getIsPrivate() != null && current.getIsPrivate() > 0;
            if (isPrivate && !admin && (requesterId == null || !requesterId.equals(current.getUserId()))) {
                log.warn(CM041003MessageConstant.LOG_QNA_PRIVATE_ACCESS_DENIED, qnaId, requesterId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(buildResponse(null, false, CM041003MessageConstant.QNA_PRIVATE_FORBIDDEN));
            }

            QnaSummary prev = cm041003Mapper.selectPrevQna(qnaId);
            QnaSummary next = cm041003Mapper.selectNextQna(qnaId);

            QnaDetailResponse detailResponse = new QnaDetailResponse(current, prev, next);
            return ResponseEntity.ok(buildResponse(detailResponse, true, CM041003MessageConstant.QNA_DETAIL_SUCCESS));
        } catch (DataAccessException dae) {
            log.error(CM041003MessageConstant.LOG_QNA_DETAIL_DB_ERROR, dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(null, false, CMMessageConstant.ERROR_DATABASE));
        } catch (Exception e) {
            log.error(CM041003MessageConstant.LOG_QNA_DETAIL_UNEXPECTED_ERROR, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(null, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseModel<QnaCreateResponse>> createQna(QnaCreateRequest request) {
        if (request == null || !StringUtils.hasText(request.getMemberNumber())) {
            log.warn(CM041003MessageConstant.LOG_QNA_CREATE_REQUEST_NULL);
            return ResponseEntity.badRequest()
                    .body(buildResponse(null, false, CM041003MessageConstant.INVALID_REQUEST));
        }

        try {
            String loginId = cm041003Mapper.findLoginIdByMemberNumber(request.getMemberNumber());
            if (!StringUtils.hasText(loginId)) {
                log.warn(CM041003MessageConstant.LOG_USER_NOT_FOUND_BY_MEMBER_NUMBER, request.getMemberNumber());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(buildResponse(null, false, CM041003MessageConstant.USER_NOT_FOUND));
            }

            String writer = truncate(loginId, 10);
            request.setCreatedBy(writer);
            request.setUpdatedBy(writer);
            int privateFlag = (request.getIsPrivate() != null && request.getIsPrivate() > 0) ? 1 : 0;
            request.setIsPrivate(privateFlag);

            if (!StringUtils.hasText(request.getInquiriesCodeType())) {
                request.setInquiriesCodeType("012");
            }
            if (StringUtils.hasText(request.getOrderNumber())) {
                request.setOrderNumber(truncate(request.getOrderNumber(), 50));
            }
            if (StringUtils.hasText(request.getProductName())) {
                request.setProductName(truncate(request.getProductName(), 150));
            }
            if (StringUtils.hasText(request.getProductId())) {
                request.setProductId(truncate(request.getProductId(), 10));
            }

            cm041003Mapper.insertQna(request);
            QnaCreateResponse response = new QnaCreateResponse(request.getQnaId());
            return ResponseEntity.ok(buildResponse(response, true, CM041003MessageConstant.QNA_CREATE_SUCCESS));
        } catch (DataAccessException dae) {
            log.error(CM041003MessageConstant.LOG_QNA_CREATE_DB_ERROR, dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(null, false, CM041003MessageConstant.QNA_CREATE_FAIL));
        } catch (Exception e) {
            log.error(CM041003MessageConstant.LOG_QNA_CREATE_UNEXPECTED_ERROR, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(null, false, CM041003MessageConstant.QNA_CREATE_FAIL));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Void>> answerQna(Long qnaId, QnaAnswerRequest request,
            Long answererId, String answererLoginId) {
        if (qnaId == null || qnaId <= 0) {
            log.warn(CM041003MessageConstant.LOG_INVALID_QNA_ID_REQUEST, qnaId);
            return ResponseEntity.badRequest()
                    .body(buildResponse(null, false, CM041003MessageConstant.INVALID_REQUEST));
        }
        if (request == null || !StringUtils.hasText(request.getAnswerContent())) {
            log.warn(CM041003MessageConstant.LOG_QNA_ANSWER_CONTENT_EMPTY, qnaId);
            return ResponseEntity.badRequest()
                    .body(buildResponse(null, false, CM041003MessageConstant.INVALID_REQUEST));
        }
        if (answererId == null) {
            log.warn(CM041003MessageConstant.LOG_QNA_ANSWERER_MISSING, qnaId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(buildResponse(null, false, CM041003MessageConstant.QNA_ANSWER_FORBIDDEN));
        }

        try {
            QnaDetail existing = cm041003Mapper.selectQnaDetail(qnaId);
            if (existing == null) {
                log.warn(CM041003MessageConstant.LOG_QNA_NOT_FOUND, qnaId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(buildResponse(null, false, CM041003MessageConstant.QNA_NOT_FOUND));
            }

            String effectiveLoginId = StringUtils.hasText(answererLoginId)
                    ? truncate(answererLoginId, 10)
                    : null;
            if (!StringUtils.hasText(effectiveLoginId)) {
                effectiveLoginId = "admin";
            }

            int inserted = cm041003Mapper.insertQnaAnswer(qnaId, request.getAnswerContent(), answererId, effectiveLoginId);
            if (inserted <= 0) {
                log.warn(CM041003MessageConstant.LOG_QNA_ANSWER_INSERT_FAIL, qnaId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(buildResponse(null, false, CM041003MessageConstant.QNA_ANSWER_FAIL));
            }

            int updated = cm041003Mapper.updateInquiryStatusToAnswered(qnaId, effectiveLoginId);
            if (updated <= 0) {
                log.warn(CM041003MessageConstant.LOG_QNA_STATUS_UPDATE_FAIL, qnaId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(buildResponse(null, false, CM041003MessageConstant.QNA_ANSWER_FAIL));
            }

            return ResponseEntity.ok(buildResponse(null, true, CM041003MessageConstant.QNA_ANSWER_SUCCESS));
        } catch (DataAccessException dae) {
            log.error(CM041003MessageConstant.LOG_QNA_ANSWER_DB_ERROR, dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(null, false, CM041003MessageConstant.QNA_ANSWER_FAIL));
        } catch (Exception e) {
            log.error(CM041003MessageConstant.LOG_QNA_ANSWER_UNEXPECTED_ERROR, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(null, false, CM041003MessageConstant.QNA_ANSWER_FAIL));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<java.util.List<QnaSummary>>> getRecentQna(Integer size, String productId) {
        final int limit = (size == null || size < 1) ? 5 : Math.min(size, 20);
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("limit", limit);
            if (StringUtils.hasText(productId)) {
                params.put("productId", productId.trim());
            }
            List<QnaSummary> items = cm041003Mapper.selectRecentQna(params);
            items = Objects.requireNonNullElse(items, Collections.emptyList());
            return ResponseEntity.ok(buildResponse(items, true, CM041003MessageConstant.QNA_LIST_SUCCESS));
        } catch (DataAccessException dae) {
            log.error(CM041003MessageConstant.LOG_RECENT_QNA_DB_ERROR, dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));
        } catch (Exception e) {
            log.error(CM041003MessageConstant.LOG_RECENT_QNA_UNEXPECTED_ERROR, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(Collections.emptyList(), false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Void>> deleteQna(Long qnaId, Long requesterId, boolean admin) {
        if (qnaId == null || qnaId <= 0) {
            return ResponseEntity.badRequest()
                    .body(buildResponse(null, false, CM041003MessageConstant.INVALID_REQUEST));
        }

        try {
            QnaDetail existing = cm041003Mapper.selectQnaDetail(qnaId);
            if (existing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(buildResponse(null, false, CM041003MessageConstant.QNA_NOT_FOUND));
            }

            boolean isOwner = requesterId != null && requesterId.equals(existing.getUserId());
            if (!admin && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(buildResponse(null, false, CM041003MessageConstant.QNA_DELETE_FORBIDDEN));
            }

            cm041003Mapper.deleteQnaAnswers(qnaId);
            int deleted = cm041003Mapper.deleteQna(qnaId);
            if (deleted <= 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(buildResponse(null, false, CM041003MessageConstant.QNA_DELETE_FAIL));
            }

            return ResponseEntity.ok(buildResponse(null, true, CM041003MessageConstant.QNA_DELETE_SUCCESS));
        } catch (DataAccessException dae) {
            log.error(CM041003MessageConstant.LOG_QNA_DELETE_DB_ERROR, dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(null, false, CMMessageConstant.ERROR_DATABASE));
        } catch (Exception e) {
            log.error(CM041003MessageConstant.LOG_QNA_DELETE_UNEXPECTED_ERROR, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(null, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    private <T> ResponseModel<T> buildResponse(T data, boolean result, String message) {
        return ResponseModel.<T>builder()
                .result(result)
                .message(message)
                .resultList(data)
                .build();
    }

    private String truncate(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
