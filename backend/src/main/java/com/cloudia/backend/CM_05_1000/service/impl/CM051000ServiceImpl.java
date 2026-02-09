package com.cloudia.backend.CM_05_1000.service.impl;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.cloudia.backend.CM_05_1000.constants.CM051000Constant;
import com.cloudia.backend.CM_05_1000.constants.CM051000MessageConstant;
import com.cloudia.backend.CM_05_1000.model.ResponseModel;
import com.cloudia.backend.CM_05_1000.mapper.CM051000Mapper;
import com.cloudia.backend.CM_05_1000.model.NoticeInfo;
import com.cloudia.backend.CM_05_1000.service.CM051000Service;
import com.cloudia.backend.constants.CMMessageConstant;
import com.cloudia.backend.config.RedisUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM051000ServiceImpl implements CM051000Service {

    private final CM051000Mapper cm051000Mapper;
    private static final long VIEW_CACHE_TTL_MS = TimeUnit.DAYS.toMillis(1);
    private final RedisUtils redisUtils;

    /**
     * 공지사항 전체 리스트 조회
     * 
     * @return 공지사항 전체 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<NoticeInfo>>> findByAllNotice() {
        log.info(CM051000MessageConstant.NOTICE_FIND_ALL_START);

        try {
            List<NoticeInfo> noticeInfoList = cm051000Mapper.findByAllNotice();
            noticeInfoList = Objects.requireNonNullElse(noticeInfoList, Collections.emptyList());

            log.info(CM051000MessageConstant.NOTICE_FIND_ALL_COMPLETE, noticeInfoList.size());
            return ResponseEntity.ok(
                    createResponseModel(noticeInfoList, true, CM051000MessageConstant.SUCCESS_NOTICE_FIND));

        } catch (DataAccessException dae) {
            log.error(CM051000MessageConstant.NOTICE_FIND_ALL_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));

        } catch (Exception e) {
            log.error(CM051000MessageConstant.NOTICE_FIND_ALL_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false,
                            CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 특정 공지사항 리스트 조회
     * 
     * @param searchKeyword 키워드
     * @param searchType    타입 (1:제목 + 내용, 2:제목, 3:내용, 4:작성자)
     * @return 공지사항 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindNotice(String searchKeyword, int searchType) {
        if (!StringUtils.hasText(searchKeyword)) {
            log.warn(CM051000MessageConstant.NOTICE_SEARCH_FAILED_EMPTY_TERM);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(Collections.emptyList(), false,
                            CM051000MessageConstant.FAIL_SEARCH_TERM_REQUIRED));
        }

        if (searchType < 1 || searchType > 4) {
            log.warn(CM051000MessageConstant.NOTICE_SEARCH_FAILED_INVALID_TYPE, searchType);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(Collections.emptyList(), false,
                            CM051000MessageConstant.FAIL_SEARCH_TYPE_INVALID));
        }

        log.info(CM051000MessageConstant.NOTICE_SEARCH_START, searchKeyword, searchType);

        try {
            List<NoticeInfo> noticeInfoList = cm051000Mapper.findByNotice(searchKeyword.trim(), searchType);
            noticeInfoList = Objects.requireNonNullElse(noticeInfoList, Collections.emptyList());

            log.info(CM051000MessageConstant.NOTICE_SEARCH_COMPLETE, noticeInfoList.size());
            return ResponseEntity.ok(
                    createResponseModel(noticeInfoList, true, CM051000MessageConstant.SUCCESS_NOTICE_FIND));

        } catch (DataAccessException dae) {
            log.error(CM051000MessageConstant.NOTICE_SEARCH_DB_ERROR, searchKeyword, searchType, dae.getMessage(),
                    dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));

        } catch (Exception e) {
            log.error(CM051000MessageConstant.NOTICE_SEARCH_UNEXPECTED_ERROR, searchKeyword, searchType,
                    e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false,
                            CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 특정 공지사항 및 이전/다음 공지사항 조회
     *
     * @param noticeId 공지사항 아이디
     * @return Map(current, prev, next) 공지사항 정보
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<java.util.Map<String, NoticeInfo>>> getFindIdNotice(int noticeId) {
        if (noticeId <= 0) {
            log.warn(CM051000MessageConstant.NOTICE_FIND_BY_ID_FAILED_INVALID_ID, noticeId);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(Collections.emptyMap(), false,
                            CM051000MessageConstant.FAIL_INVALID_NOTICE_ID));
        }

        log.info(CM051000MessageConstant.NOTICE_FIND_BY_ID_START, noticeId);

        try {
            NoticeInfo current = cm051000Mapper.findIdNoticeOne(noticeId);
            NoticeInfo prev = cm051000Mapper.findPrevNotice(noticeId);
            NoticeInfo next = cm051000Mapper.findNextNotice(noticeId);

            java.util.Map<String, NoticeInfo> result = new java.util.HashMap<>();
            result.put("current", current);
            result.put("prev", prev);
            result.put("next", next);

            log.info(CM051000MessageConstant.NOTICE_FIND_BY_ID_COMPLETE, noticeId);
            return ResponseEntity.ok(createResponseModel(result, true, CM051000MessageConstant.SUCCESS_NOTICE_FIND));

        } catch (DataAccessException dae) {
            log.error(CM051000MessageConstant.NOTICE_FIND_BY_ID_DB_ERROR, noticeId, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyMap(), false, CMMessageConstant.ERROR_DATABASE));

        } catch (Exception e) {
            log.error(CM051000MessageConstant.NOTICE_FIND_BY_ID_UNEXPECTED_ERROR, noticeId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyMap(), false,
                            CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 공지사항 조회수 증가 (하루 1회 제한)
     *
     * @param noticeId 공지사항 ID
     * @param viewerKey 뷰어 키
     * @return 증가 여부
     */
    @Override
    @Transactional
    public boolean increaseViewOncePerDay(int noticeId, String viewerKey) {
        if (viewerKey == null || viewerKey.isBlank()) {
            log.warn("Viewer key is missing. noticeId={}", noticeId);
            return false;
        }

        final String cacheKey = String.format("notice:view:%d:%s", noticeId, viewerKey);
        boolean firstVisit = redisUtils.setIfAbsent(cacheKey, "Y", VIEW_CACHE_TTL_MS);
        if (!firstVisit) {
            log.debug(CM051000MessageConstant.NOTICE_VIEW_ALREADY_COUNTED, cacheKey);
            return false;
        }

        try {
            int updatedRows = cm051000Mapper.incrementViewCount(noticeId);
            if (updatedRows > 0) {
                log.info(CM051000MessageConstant.NOTICE_VIEW_INCREMENT_SUCCESS, noticeId);
                return true;
            }

            redisUtils.deleteData(cacheKey);
            log.warn(CM051000MessageConstant.NOTICE_VIEW_INCREMENT_FAIL, noticeId);
            return false;
        } catch (Exception e) {
            redisUtils.deleteData(cacheKey);
            log.error(CM051000MessageConstant.NOTICE_VIEW_INCREMENT_ERROR, noticeId, e);
            throw e;
        }
    }

    /**
     * 공지사항 등록
     * 
     * @param entity 등록 할 공지사항 정보
     * @return 등록 여부
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Integer>> noticeUpload(NoticeInfo entity) {
        if (entity == null) {
            log.warn(CM051000MessageConstant.NOTICE_UPLOAD_FAILED_EMPTY_LIST);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(0, false, CM051000MessageConstant.FAIL_NO_NOTICE_SELECTED));
        }
        log.info(CM051000MessageConstant.NOTICE_UPLOAD_START, entity.getTitle());
        try {
            int result = insertNotice(entity);

            log.info(CM051000MessageConstant.NOTICE_UPLOAD_COMPLETE, entity.getTitle(), result);
            return ResponseEntity.ok(
                    createResponseModel(result, true, CM051000MessageConstant.SUCCESS_NOTICE_UPLOAD));

        } catch (DuplicateKeyException dke) {
            log.error(CM051000MessageConstant.NOTICE_UPLOAD_DUPLICATE_KEY_ERROR, dke.getMessage(), dke);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createResponseModel(0, false, CM051000MessageConstant.FAIL_DUPLICATE_NOTICE_TITLE));

        } catch (DataIntegrityViolationException dive) {
            log.error(CM051000MessageConstant.NOTICE_UPLOAD_DB_ERROR, dive.getMessage(), dive);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(0, false, CM051000MessageConstant.FAIL_DUPLICATE_NOTICE_INFO));

        } catch (DataAccessException dae) {
            log.error(CM051000MessageConstant.NOTICE_UPLOAD_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_DATABASE));

        } catch (Exception e) {
            log.error(CM051000MessageConstant.NOTICE_UPLOAD_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 공지사항 업데이트
     * 
     * @param entity 업데이트 할 공지사항 정보
     * @return 업데이트 여부
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Integer>> noticeUpdate(NoticeInfo entity) {
        if (entity == null) {
            log.warn(CM051000MessageConstant.NOTICE_UPDATE_FAILED_EMPTY_LIST);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(0, false, CM051000MessageConstant.FAIL_NO_NOTICE_SELECTED));
        }
        log.info(CM051000MessageConstant.NOTICE_UPDATE_START, entity.getNoticeId(), entity.getTitle());
        try {
            int result = updateNotice(entity);

            if (result == 0) {
                log.warn(CM051000MessageConstant.NOTICE_UPDATE_FAILED_NOT_EXISTS, entity.getNoticeId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createResponseModel(0, false, CM051000MessageConstant.FAIL_NOTICE_NOT_EXISTS));
            }

            log.info(CM051000MessageConstant.NOTICE_UPDATE_COMPLETE, entity.getNoticeId(), result);
            return ResponseEntity.ok(
                    createResponseModel(result, true, CM051000MessageConstant.SUCCESS_NOTICE_UPDATE));

        } catch (DuplicateKeyException dke) {
            log.error(CM051000MessageConstant.NOTICE_UPDATE_DUPLICATE_KEY_ERROR, dke.getMessage(), dke);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createResponseModel(0, false, CM051000MessageConstant.FAIL_DUPLICATE_NOTICE_TITLE));

        } catch (DataIntegrityViolationException dive) {
            log.error(CM051000MessageConstant.NOTICE_UPDATE_DB_ERROR, dive.getMessage(), dive);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(0, false, CM051000MessageConstant.FAIL_DUPLICATE_NOTICE_UPDATE));

        } catch (DataAccessException dae) {
            log.error(CM051000MessageConstant.NOTICE_UPDATE_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_DATABASE));

        } catch (Exception e) {
            log.error(CM051000MessageConstant.NOTICE_UPDATE_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 공지사항 등록 실행
     * 
     * @param entity 공지사항 정보
     * @return 등록 결과
     */
    private int insertNotice(NoticeInfo entity) {
        NoticeInfo noticeModel = createNoticeModel(entity);
        noticeModel.setPublishedAt(new Date(System.currentTimeMillis()));
        noticeModel.setUserId("1");
        noticeModel.setCreatedBy("1");
        noticeModel.setCreatedAt(LocalDateTime.now());

        return cm051000Mapper.noticeUpload(noticeModel);
    }

    /**
     * 공지사항 업데이트 실행
     * 
     * @param entity 공지사항 정보
     * @return 업데이트 결과
     */
    private int updateNotice(NoticeInfo entity) {
        NoticeInfo noticeModel = createNoticeModel(entity);
        noticeModel.setNoticeId(entity.getNoticeId());

        return cm051000Mapper.noticeUpdate(noticeModel);
    }

    /**
     * 공지사항 모델 생성 (공통 로직)
     * 
     * @param entity 원본 엔티티
     * @return NoticeInfo 모델
     */
    private NoticeInfo createNoticeModel(NoticeInfo entity) {
        NoticeInfo noticeModel = new NoticeInfo();

        noticeModel.setTitle(entity.getTitle().trim());
        noticeModel.setContent(entity.getContent());
        noticeModel.setCodeValue(entity.getCodeValue());
        noticeModel.setPinned(entity.getCodeValue() == CM051000Constant.GENERAL_INFO
                ? CM051000Constant.NOT_FIXED_TOP
                : CM051000Constant.FIXED_TOP);
        noticeModel.setIsDisplay(entity.getIsDisplay());
        noticeModel.setUpdatedBy("1");
        noticeModel.setUpdatedAt(LocalDateTime.now());

        return noticeModel;
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
     * 공지사항 삭제
     *
     * @param noticeId 삭제할 공지사항 ID
     * @return 삭제 결과
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Integer>> deleteNotice(Long noticeId) {
        if (noticeId == null || noticeId <= 0) {
            log.warn(CM051000MessageConstant.NOTICE_DELETE_FAILED_INVALID_ID, noticeId);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(0, false, CM051000MessageConstant.FAIL_INVALID_NOTICE_ID));
        }

        log.info(CM051000MessageConstant.NOTICE_DELETE_START, noticeId);

        try {
            int result = cm051000Mapper.deleteNotice(noticeId);

            if (result == 0) {
                log.warn(CM051000MessageConstant.NOTICE_DELETE_FAILED_NOT_EXISTS, noticeId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createResponseModel(0, false, CM051000MessageConstant.FAIL_NOTICE_NOT_EXISTS));
            }

            log.info(CM051000MessageConstant.NOTICE_DELETE_COMPLETE, noticeId, result);
            return ResponseEntity.ok(
                    createResponseModel(result, true, CM051000MessageConstant.SUCCESS_NOTICE_DELETE));

        } catch (DataAccessException dae) {
            log.error(CM051000MessageConstant.NOTICE_DELETE_DB_ERROR, noticeId, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_DATABASE));

        } catch (Exception e) {
            log.error(CM051000MessageConstant.NOTICE_DELETE_UNEXPECTED_ERROR, noticeId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }
}
