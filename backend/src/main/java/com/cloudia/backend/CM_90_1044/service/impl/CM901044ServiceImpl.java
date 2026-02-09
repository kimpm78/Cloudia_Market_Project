package com.cloudia.backend.CM_90_1044.service.impl;

import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.cloudia.backend.CM_90_1044.constants.CM901044Constant;
import com.cloudia.backend.CM_90_1044.constants.CM901044MessageConstant;
import com.cloudia.backend.CM_90_1044.model.ResponseModel;
import com.cloudia.backend.CM_90_1044.mapper.CM901044Mapper;
import com.cloudia.backend.CM_90_1044.model.NoticeInfo;
import com.cloudia.backend.CM_90_1044.service.CM901044Service;
import com.cloudia.backend.common.exception.AuthenticationException;
import com.cloudia.backend.common.exception.ErrorCode;
import com.cloudia.backend.common.log.LogHelper;
import com.cloudia.backend.common.log.LogMessage;
import com.cloudia.backend.common.util.DateCalculator;
import com.cloudia.backend.constants.CMMessageConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM901044ServiceImpl implements CM901044Service {

    private final CM901044Mapper cm901044Mapper;
    private final DateCalculator dateCalculator;

    /**
     * 공지사항 전체 리스트 조회
     * 
     * @return 공지사항 전체 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<NoticeInfo>>> findByAllNotice() {
        log.info(CM901044MessageConstant.NOTICE_FIND_ALL_START);

        try {
            List<NoticeInfo> noticeInfoList = cm901044Mapper.findByAllNotice();
            noticeInfoList = Objects.requireNonNullElse(noticeInfoList, Collections.emptyList());

            log.info(CM901044MessageConstant.NOTICE_FIND_ALL_COMPLETE, noticeInfoList.size());
            return ResponseEntity.ok(
                    createResponseModel(noticeInfoList, true, CM901044MessageConstant.SUCCESS_NOTICE_FIND));

        } catch (DataAccessException dae) {
            log.error(CM901044MessageConstant.NOTICE_FIND_ALL_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));

        } catch (Exception e) {
            log.error(CM901044MessageConstant.NOTICE_FIND_ALL_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false,
                            CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 특정 공지사항 리스트 조회
     * 
     * @param searchKeyword 키워드
     * @param searchType    타입 (1:제목 + 내용, 2:제목, 3:내용)
     * @return 공지사항 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindNotice(String searchKeyword, int searchType) {
        if (!StringUtils.hasText(searchKeyword)) {
            log.warn(CM901044MessageConstant.NOTICE_SEARCH_FAILED_EMPTY_TERM);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(Collections.emptyList(), false,
                            CM901044MessageConstant.FAIL_SEARCH_TERM_REQUIRED));
        }

        if (searchType < 1 || searchType > 3) {
            log.warn(CM901044MessageConstant.NOTICE_SEARCH_FAILED_INVALID_TYPE, searchType);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(Collections.emptyList(), false,
                            CM901044MessageConstant.FAIL_SEARCH_TYPE_INVALID));
        }

        log.info(CM901044MessageConstant.NOTICE_SEARCH_START, searchKeyword, searchType);

        try {
            List<NoticeInfo> noticeInfoList = cm901044Mapper.findByNotice(searchKeyword.trim(), searchType);
            noticeInfoList = Objects.requireNonNullElse(noticeInfoList, Collections.emptyList());

            log.info(CM901044MessageConstant.NOTICE_SEARCH_COMPLETE, noticeInfoList.size());
            return ResponseEntity.ok(
                    createResponseModel(noticeInfoList, true, CM901044MessageConstant.SUCCESS_NOTICE_FIND));

        } catch (DataAccessException dae) {
            log.error(CM901044MessageConstant.NOTICE_SEARCH_DB_ERROR, searchKeyword, searchType, dae.getMessage(),
                    dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));

        } catch (Exception e) {
            log.error(CM901044MessageConstant.NOTICE_SEARCH_UNEXPECTED_ERROR, searchKeyword, searchType,
                    e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false,
                            CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 특정 공지사항 리스트 조회
     * 
     * @param noticeId 공지사항 아이디
     * @return 공지사항 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindIdNotice(int noticeId) {
        if (noticeId <= 0) {
            log.warn(CM901044MessageConstant.NOTICE_FIND_BY_ID_FAILED_INVALID_ID, noticeId);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(Collections.emptyList(), false,
                            CM901044MessageConstant.FAIL_INVALID_NOTICE_ID));
        }

        log.info(CM901044MessageConstant.NOTICE_FIND_BY_ID_START, noticeId);

        try {
            List<NoticeInfo> noticeInfoList = cm901044Mapper.findIdNotice(noticeId);
            noticeInfoList = Objects.requireNonNullElse(noticeInfoList, Collections.emptyList());

            log.info(CM901044MessageConstant.NOTICE_FIND_BY_ID_COMPLETE, noticeId, noticeInfoList.size());
            return ResponseEntity.ok(
                    createResponseModel(noticeInfoList, true, CM901044MessageConstant.SUCCESS_NOTICE_FIND));

        } catch (DataAccessException dae) {
            log.error(CM901044MessageConstant.NOTICE_FIND_BY_ID_DB_ERROR, noticeId, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));

        } catch (Exception e) {
            log.error(CM901044MessageConstant.NOTICE_FIND_BY_ID_UNEXPECTED_ERROR, noticeId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false,
                            CMMessageConstant.ERROR_INTERNAL_SERVER));
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
    public ResponseEntity<ResponseModel<Integer>> noticeUpload(NoticeInfo entity, String userId) {
        if (entity == null) {
            log.warn(CM901044MessageConstant.NOTICE_UPLOAD_FAILED_EMPTY_LIST);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(0, false, CM901044MessageConstant.FAIL_NO_NOTICE_SELECTED));
        }
        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "공지사항 조회" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }
        log.info(CM901044MessageConstant.NOTICE_UPLOAD_START, entity.getTitle());

        try {
            int result = insertNotice(entity, userId);

            log.info(CM901044MessageConstant.NOTICE_UPLOAD_COMPLETE, entity.getTitle(), result);
            return ResponseEntity.ok(
                    createResponseModel(result, true, CM901044MessageConstant.SUCCESS_NOTICE_UPLOAD));

        } catch (DuplicateKeyException dke) {
            log.error(CM901044MessageConstant.NOTICE_UPLOAD_DUPLICATE_KEY_ERROR, dke.getMessage(), dke);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createResponseModel(0, false, CM901044MessageConstant.FAIL_DUPLICATE_NOTICE_TITLE));

        } catch (DataIntegrityViolationException dive) {
            log.error(CM901044MessageConstant.NOTICE_UPLOAD_DB_ERROR, dive.getMessage(), dive);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(0, false, CM901044MessageConstant.FAIL_DUPLICATE_NOTICE_INFO));

        } catch (DataAccessException dae) {
            log.error(CM901044MessageConstant.NOTICE_UPLOAD_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_DATABASE));

        } catch (Exception e) {
            log.error(CM901044MessageConstant.NOTICE_UPLOAD_UNEXPECTED_ERROR, e.getMessage(), e);
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
    public ResponseEntity<ResponseModel<Integer>> noticeUpdate(NoticeInfo entity, String userId) {
        if (entity == null) {
            log.warn(CM901044MessageConstant.NOTICE_UPDATE_FAILED_EMPTY_LIST);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(0, false, CM901044MessageConstant.FAIL_NO_NOTICE_SELECTED));
        }
        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "공지사항 조회" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }
        log.info(CM901044MessageConstant.NOTICE_UPDATE_START, entity.getNoticeId(), entity.getTitle());

        try {
            int result = updateNotice(entity, userId);

            if (result == 0) {
                log.warn(CM901044MessageConstant.NOTICE_UPDATE_FAILED_NOT_EXISTS, entity.getNoticeId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createResponseModel(0, false, CM901044MessageConstant.FAIL_NOTICE_NOT_EXISTS));
            }

            log.info(CM901044MessageConstant.NOTICE_UPDATE_COMPLETE, entity.getNoticeId(), result);
            return ResponseEntity.ok(
                    createResponseModel(result, true, CM901044MessageConstant.SUCCESS_NOTICE_UPDATE));

        } catch (DuplicateKeyException dke) {
            log.error(CM901044MessageConstant.NOTICE_UPDATE_DUPLICATE_KEY_ERROR, dke.getMessage(), dke);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createResponseModel(0, false, CM901044MessageConstant.FAIL_DUPLICATE_NOTICE_TITLE));

        } catch (DataIntegrityViolationException dive) {
            log.error(CM901044MessageConstant.NOTICE_UPDATE_DB_ERROR, dive.getMessage(), dive);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(0, false, CM901044MessageConstant.FAIL_DUPLICATE_NOTICE_UPDATE));

        } catch (DataAccessException dae) {
            log.error(CM901044MessageConstant.NOTICE_UPDATE_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_DATABASE));

        } catch (Exception e) {
            log.error(CM901044MessageConstant.NOTICE_UPDATE_UNEXPECTED_ERROR, e.getMessage(), e);
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
    private int insertNotice(NoticeInfo entity, String userId) {
        NoticeInfo noticeModel = createNoticeModel(entity, userId);
        noticeModel.setPublishedAt(new Date(System.currentTimeMillis()));
        noticeModel.setUserId(userId);
        noticeModel.setCreatedBy(userId);
        noticeModel.setCreatedAt(dateCalculator.tokyoTime());

        return cm901044Mapper.noticeUpload(noticeModel);
    }

    /**
     * 공지사항 업데이트 실행
     * 
     * @param entity 공지사항 정보
     * @return 업데이트 결과
     */
    private int updateNotice(NoticeInfo entity, String userId) {
        NoticeInfo noticeModel = createNoticeModel(entity, userId);
        noticeModel.setNoticeId(entity.getNoticeId());

        return cm901044Mapper.noticeUpdate(noticeModel);
    }

    /**
     * 공지사항 모델 생성 (공통 로직)
     * 
     * @param entity 원본 엔티티
     * @return NoticeInfo 모델
     */
    private NoticeInfo createNoticeModel(NoticeInfo entity, String userId) {
        NoticeInfo noticeModel = new NoticeInfo();

        noticeModel.setTitle(entity.getTitle().trim());
        noticeModel.setContent(entity.getContent());
        noticeModel.setCodeValue(entity.getCodeValue());
        noticeModel.setPinned(entity.getCodeValue() == CM901044Constant.GENERAL_INFO
                ? CM901044Constant.NOT_FIXED_TOP
                : CM901044Constant.FIXED_TOP);
        noticeModel.setIsDisplay(entity.getIsDisplay());
        noticeModel.setUpdatedBy(userId);
        noticeModel.setUpdatedAt(dateCalculator.tokyoTime());

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
}