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
import com.cloudia.backend.CM_90_1044.mapper.CM901044Mapper;
import com.cloudia.backend.CM_90_1044.model.NoticeInfo;
import com.cloudia.backend.CM_90_1044.service.CM901044Service;
import com.cloudia.backend.common.model.ResponseModel;
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
     * お知らせ全件リスト取得
     *
     * @return お知らせ全件リスト
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
     * お知らせ検索（条件指定）
     *
     * @param searchKeyword キーワード
     * @param searchType    タイプ（1:タイトル+内容, 2:タイトル, 3:内容）
     * @return お知らせリスト
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
     * お知らせID指定取得
     *
     * @param noticeId お知らせID
     * @return お知らせリスト
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
     * お知らせ登録
     *
     * @param entity 登録するお知らせ情報
     * @return 登録結果
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
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "お知らせ照会" });
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
     * お知らせ更新
     *
     * @param entity 更新するお知らせ情報
     * @return 更新結果
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
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "お知らせ照会" });
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
     * お知らせ登録処理（実行）
     *
     * @param entity お知らせ情報
     * @return 登録結果
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
     * お知らせ更新処理（実行）
     *
     * @param entity お知らせ情報
     * @return 更新結果
     */
    private int updateNotice(NoticeInfo entity, String userId) {
        NoticeInfo noticeModel = createNoticeModel(entity, userId);
        noticeModel.setNoticeId(entity.getNoticeId());

        return cm901044Mapper.noticeUpdate(noticeModel);
    }

    /**
     * お知らせモデル生成（共通ロジック）
     *
     * @param entity 元エンティティ
     * @return NoticeInfo モデル
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
}