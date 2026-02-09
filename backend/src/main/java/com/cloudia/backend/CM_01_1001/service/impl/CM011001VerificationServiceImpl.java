package com.cloudia.backend.CM_01_1001.service.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_01_1001.constants.CM011001MessageConstant;
import com.cloudia.backend.CM_01_1001.mapper.CM011001UserMapper;
import com.cloudia.backend.CM_01_1001.service.CM011001VerificationService;
import com.cloudia.backend.common.model.EmailDto;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.service.EmailService;
import com.cloudia.backend.constants.CMMessageConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CM011001VerificationServiceImpl implements CM011001VerificationService {

    private final CM011001UserMapper userMapper;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${verification.code.expiration.minutes}")
    private long expirationMinutes;

    private static final String EMAIL_VERIFICATION_CODE_PREFIX = "email:verification:code:";
    private static final String EMAIL_VERIFIED_STATUS_PREFIX = "email:verification:status:";

    /**
     * 이메일 주소로 인증 코드를 발송하고 Redis에 저장
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Map<String, String>>> sendVerificationEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            log.warn(CM011001MessageConstant.FAIL_EMAIL_REQUIRED);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(Collections.emptyMap(), false,
                            CM011001MessageConstant.FAIL_EMAIL_REQUIRED));
        }

        log.info(CM011001MessageConstant.EMAIL_SEND_START, email);
        try {
            // 이메일 중복 체크
            if (userMapper.countByEmail(email) > 0) {
                log.warn(CM011001MessageConstant.SIGNUP_WARN_EMAIL_CONFLICT, email);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createResponseModel(Collections.emptyMap(), false,
                                CM011001MessageConstant.FAIL_EMAIL_CONFLICT));
            }

            // 인증 코드 생성
            String verificationCode = emailService.generateVerificationCode();
            // Redis에 인증 코드 저장
            redisTemplate.opsForValue().set(EMAIL_VERIFICATION_CODE_PREFIX + email, verificationCode,
                    expirationMinutes, TimeUnit.MINUTES);

            // EmailDto 생성 및 발송
            EmailDto emailInfo = new EmailDto();
            emailInfo.setSendEmail(email);
            emailInfo.setVerificationCode(verificationCode);

            emailService.sendVerificationEmail(emailInfo);

            log.info(CM011001MessageConstant.EMAIL_SEND_SUCCESS_LOG, email, verificationCode);

            return ResponseEntity.ok(createResponseModel(
                    Map.of("email", email),
                    true,
                    CM011001MessageConstant.SUCCESS_EMAIL_SEND));

        } catch (RedisConnectionFailureException redisEx) {
            log.error(CM011001MessageConstant.EMAIL_SEND_REDIS_ERROR, redisEx.getMessage(), redisEx);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyMap(), false,
                            CM011001MessageConstant.FAIL_REDIS_CONNECTION));

        } catch (DataAccessException dae) {
            log.error(CM011001MessageConstant.EMAIL_SEND_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyMap(), false, CMMessageConstant.ERROR_DATABASE));

        } catch (Exception e) {
            log.error(CM011001MessageConstant.EMAIL_SEND_SERVICE_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyMap(), false,
                            CM011001MessageConstant.FAIL_EMAIL_SEND));
        }
    }

    /**
     * 입력된 인증 코드를 검증
     */
    @Override
    public ResponseEntity<ResponseModel<Map<String, Object>>> verifyEmail(String email, String code) {
        log.info(CM011001MessageConstant.EMAIL_VERIFY_START, email);

        try {
            String key = EMAIL_VERIFICATION_CODE_PREFIX + email;
            String storedCode = redisTemplate.opsForValue().get(key);

            // 코드가 없거나 만료된 경우
            if (storedCode == null) {
                log.warn(CM011001MessageConstant.EMAIL_VERIFY_WARN_NOT_EXIST_OR_EXPIRED, email);
                return ResponseEntity.badRequest().body(
                        createResponseModel(Map.of("verified", false), false,
                                CM011001MessageConstant.FAIL_INVALID_VERIFICATION_CODE));
            }

            // 코드가 일치하는 경우
            if (storedCode.equals(code)) {
                redisTemplate.delete(key); // 인증 코드 삭제

                // 인증 완료 상태 저장 (30분 유효)
                redisTemplate.opsForValue().set(EMAIL_VERIFIED_STATUS_PREFIX + email, "true", 30, TimeUnit.MINUTES);

                log.info(CM011001MessageConstant.EMAIL_VERIFY_SUCCESS_LOG, email);
                return ResponseEntity.ok(createResponseModel(
                        Map.of("verified", true),
                        true,
                        CM011001MessageConstant.SUCCESS_EMAIL_VERIFY));
            } else {
                // 코드가 일치하는 않은 경우
                log.warn(CM011001MessageConstant.EMAIL_VERIFY_WARN_CODE_MISMATCH, email, code, storedCode);
                return ResponseEntity.badRequest().body(
                        createResponseModel(Map.of("verified", false), false,
                                CM011001MessageConstant.FAIL_CODE_MISMATCH));
            }

        } catch (RedisConnectionFailureException redisEx) {
            log.error(CM011001MessageConstant.EMAIL_VERIFY_REDIS_ERROR, redisEx.getMessage(), redisEx);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyMap(), false,
                            CM011001MessageConstant.FAIL_REDIS_CONNECTION));

        } catch (Exception e) {
            log.error(CM011001MessageConstant.EMAIL_VERIFY_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyMap(), false,
                            CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    @Override
    public boolean isEmailVerified(String email) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(EMAIL_VERIFIED_STATUS_PREFIX + email));
        } catch (Exception e) {
            log.error(CM011001MessageConstant.EMAIL_REDIS_CHECK_ERROR, email, e);
            return false;
        }
    }

    @Override
    public void clearVerificationStatus(String email) {
        try {
            redisTemplate.delete(EMAIL_VERIFIED_STATUS_PREFIX + email);
            log.info(CM011001MessageConstant.EMAIL_VERIFY_STATUS_CLEARED_LOG, email);
        } catch (Exception e) {
            log.warn(CM011001MessageConstant.EMAIL_REDIS_CLEAR_ERROR, email, e);
        }
    }

    private <T> ResponseModel<T> createResponseModel(T resultList, boolean result, String message) {
        return ResponseModel.<T>builder()
                .resultList(resultList)
                .result(result)
                .message(Objects.requireNonNull(message, CMMessageConstant.MESSAGE_NULL_NOT_ALLOWED))
                .build();
    }
}