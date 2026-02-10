package com.cloudia.backend.CM_90_1031.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_90_1031.constants.CM901031MessageConstant;
import com.cloudia.backend.CM_90_1031.mapper.CM901031Mapper;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_90_1031.model.ageDto;
import com.cloudia.backend.CM_90_1031.model.genderDto;
import com.cloudia.backend.CM_90_1031.service.CM901031Service;
import com.cloudia.backend.constants.CMMessageConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM901031ServiceImpl implements CM901031Service {

    private final CM901031Mapper cm901031Mapper;
    /**
     * 年齢層一覧取得
     * 
     * @return 年齢層一覧
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<ageDto>>> findByAllAges() {
        log.info(CM901031MessageConstant.USER_FIND_ALL_START);
        try {
            List<ageDto> responseUserList = cm901031Mapper.findByAllAges();

            if (responseUserList == null) {
                responseUserList = Collections.emptyList();
            }

            log.info(CM901031MessageConstant.USER_FIND_ALL_COMPLETE,
                    responseUserList == null ? 0 : responseUserList.size());

            return ResponseEntity
                    .ok(createResponseModel(responseUserList, true, CM901031MessageConstant.SUCCESS_USER_FIND));
        } catch (DataAccessException dae) {
            // DB関連例外
            log.error(CM901031MessageConstant.USER_FIND_ALL_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));

        } catch (NullPointerException npe) {
            // Null処理例外
            log.error(CM901031MessageConstant.USER_FIND_ALL_NULL_ERROR, npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_NULL));

        } catch (Exception e) {
            // その他の一般例外
            log.error(CM901031MessageConstant.USER_FIND_ALL_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false,
                            CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 性別一覧取得
     * 
     * @return 性別一覧
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<genderDto>>> findByAllGenders() {
        log.info(CM901031MessageConstant.USER_FIND_ALL_START);
        try {
            List<genderDto> responseUserList = cm901031Mapper.findByAllGenders();

            if (responseUserList == null) {
                responseUserList = Collections.emptyList();
            }

            log.info(CM901031MessageConstant.USER_FIND_ALL_COMPLETE,
                    responseUserList == null ? 0 : responseUserList.size());

            return ResponseEntity
                    .ok(createResponseModel(responseUserList, true, CM901031MessageConstant.SUCCESS_USER_FIND));
        } catch (DataAccessException dae) {
            // DB関連例外
            log.error(CM901031MessageConstant.USER_FIND_ALL_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));

        } catch (NullPointerException npe) {
            // Null処理例外
            log.error(CM901031MessageConstant.USER_FIND_ALL_NULL_ERROR, npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_NULL));

        } catch (Exception e) {
            // その他の一般例外
            log.error(CM901031MessageConstant.USER_FIND_ALL_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false,
                            CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * ResponseModel生成
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
