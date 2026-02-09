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
import com.cloudia.backend.CM_90_1031.model.ResponseModel;
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
     * 연령대 리스트 조회
     * 
     * @return 연령대 리스트
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
            // DB 관련 예외
            log.error(CM901031MessageConstant.USER_FIND_ALL_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));

        } catch (NullPointerException npe) {
            // Null 처리 예외
            log.error(CM901031MessageConstant.USER_FIND_ALL_NULL_ERROR, npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_NULL));

        } catch (Exception e) {
            // 그 외 일반 예외
            log.error(CM901031MessageConstant.USER_FIND_ALL_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false,
                            CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 연령대 리스트 조회
     * 
     * @return 연령대 리스트
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
            // DB 관련 예외
            log.error(CM901031MessageConstant.USER_FIND_ALL_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));

        } catch (NullPointerException npe) {
            // Null 처리 예외
            log.error(CM901031MessageConstant.USER_FIND_ALL_NULL_ERROR, npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_NULL));

        } catch (Exception e) {
            // 그 외 일반 예외
            log.error(CM901031MessageConstant.USER_FIND_ALL_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false,
                            CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
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
