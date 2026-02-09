package com.cloudia.backend.CM_01_1002.service.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_01_1002.constants.CM011002MesaageConstant;
import com.cloudia.backend.CM_01_1002.mapper.CM011002Mapper;
import com.cloudia.backend.CM_01_1002.service.CM011002Service;
import com.cloudia.backend.common.model.EmailDto;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.service.EmailService;
import com.cloudia.backend.constants.CMMessageConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM011002ServiceImpl implements CM011002Service {

        private final CM011002Mapper cm011002Mapper;
        private final EmailService emailService;
        private final RedisTemplate<String, String> redisTemplate;

        @Value("${verification.code.expiration.minutes}")
        private long expirationMinutes;

        private static final String EMAIL_VERIFICATION_PREFIX = "email:verification:";

        @Override
        public ResponseEntity<ResponseModel<Map<String, String>>> sendVerificationCodeForFindId(String email) {
        // メールアドレスの形式を検証
        if (email == null || !Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", email)) {
            log.warn(CM011002MesaageConstant.FIND_ID_INVALID_EMAIL_FORMAT_LOG, email);
            return ResponseEntity.badRequest()
            .body(createResponseModel(Collections.emptyMap(), false,
                CM011002MesaageConstant.FIND_ID_INVALID_EMAIL_FORMAT));
        }
        // 登録されているメールアドレスか確認
        if (cm011002Mapper.countByEmail(email) == 0) {
            log.warn(CM011002MesaageConstant.FIND_ID_EMAIL_NOT_REGISTERED_LOG, email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(createResponseModel(Collections.emptyMap(), false,
            CM011002MesaageConstant.FIND_ID_EMAIL_NOT_REGISTERED));
        }
        try {
        // 인증 코드 생성
        String verificationCode = emailService.generateVerificationCode();

        // Redis 저장
        redisTemplate.opsForValue().set(EMAIL_VERIFICATION_PREFIX + email, verificationCode,
                        expirationMinutes, TimeUnit.MINUTES);

        // メールDTOの作成および送信
        EmailDto emailInfo = new EmailDto();
        emailInfo.setSendEmail(email);
        emailInfo.setVerificationCode(verificationCode);

        emailInfo.setSendEmail(email);
        emailInfo.setVerificationCode(verificationCode);
        emailService.sendFindIdVerificationEmail(emailInfo);

        log.info(CM011002MesaageConstant.FIND_ID_EMAIL_SEND_SUCCESS_LOG, email, verificationCode);

        return ResponseEntity.ok(createResponseModel(
            Map.of("message", CM011002MesaageConstant.FIND_ID_EMAIL_SEND_SUCCESS),true,
            CM011002MesaageConstant.FIND_ID_EMAIL_SEND_SUCCESS));

        } catch (Exception e) {
            log.error(CM011002MesaageConstant.FIND_ID_EMAIL_SEND_SERVICE_ERROR_LOG, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createResponseModel(Collections.emptyMap(), false,CM011002MesaageConstant.FIND_ID_EMAIL_SEND_FAILED));
            }
        }

        @Override
        public ResponseEntity<ResponseModel<Map<String, Object>>> verifyAndFindId(String email, String code) {
            String key = EMAIL_VERIFICATION_PREFIX + email;
            String storedCode = redisTemplate.opsForValue().get(key);

            if (storedCode == null) {
                log.warn(CM011002MesaageConstant.FIND_ID_VERIFY_EXPIRED_LOG, email);
                return ResponseEntity.badRequest()
                    .body(createResponseModel(Collections.emptyMap(), false,CM011002MesaageConstant.FIND_ID_VERIFY_EXPIRED));
            }

            if (!storedCode.equals(code)) {
                log.warn(CM011002MesaageConstant.FIND_ID_VERIFY_MISMATCH_LOG, email, code, storedCode);
                return ResponseEntity.badRequest()
                    .body(createResponseModel(Collections.emptyMap(), false,CM011002MesaageConstant.FIND_ID_VERIFY_MISMATCH));
            }

                redisTemplate.delete(key);

                User user = cm011002Mapper.findByEmail(email);
                if (user == null) {
                    log.error(CM011002MesaageConstant.FIND_ID_USER_NOT_FOUND_FATAL_ERROR_LOG, email);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createResponseModel(Collections.emptyMap(), false,
                        CM011002MesaageConstant.FIND_ID_USER_NOT_FOUND_AFTER_VERIFY));
                }

                Map<String, Object> responseBody = Map.of(
                    "verified", true,
                    "loginId", user.getLoginId(),
                    "message", "認証に成功しました。");

                return ResponseEntity.ok(createResponseModel(responseBody, true, "認証成功"));
        }

        private <T> ResponseModel<T> createResponseModel(T resultList, boolean result, String message) {
                return ResponseModel.<T>builder()
            .resultList(resultList)
            .result(result)
            .message(Objects.requireNonNull(message, CMMessageConstant.MESSAGE_NULL_NOT_ALLOWED))
            .build();
        }
}