package com.cloudia.backend.CM_01_1003.service.impl;

import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_01_1003.constants.CM011003MessageConstant;
import com.cloudia.backend.CM_01_1003.mapper.CM011003Mapper;
import com.cloudia.backend.CM_01_1003.model.ResetPasswordRequest;
import com.cloudia.backend.CM_01_1003.service.CM011003Service;
import com.cloudia.backend.auth.mapper.PasswordHistoryMapper;
import com.cloudia.backend.auth.model.PasswordHistory;
import com.cloudia.backend.common.model.EmailDto;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.service.EmailService;
import com.cloudia.backend.constants.CMMessageConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM011003ServiceImpl implements CM011003Service {

    private final CM011003Mapper mapper;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final PasswordHistoryMapper passwordHistoryMapper;

    @Value("${verification.code.expiration.minutes}")
    private long expirationMinutes;

    private static final String PW_VERIFICATION_PREFIX = "pw-verification:";

    @Override
    public ResponseEntity<ResponseModel<Map<String, Object>>> sendVerificationCodeForFindPw(
            ResetPasswordRequest request) {
        String email = request.getEmail();

        try {
            // メールアドレスでユーザーの存在有無のみを確認
            User user = mapper.findByEmail(email);
            if (user == null) {
                log.error(CM011003MessageConstant.USER_NOT_FOUND_LOG, email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createResponseModel(Collections.emptyMap(), false,
                                CM011003MessageConstant.USER_NOT_FOUND));
            }

            // 認証コード生成
            String code = emailService.generateVerificationCode();

            // Redis 格納
            redisTemplate.opsForValue().set(PW_VERIFICATION_PREFIX + email, code, expirationMinutes, TimeUnit.MINUTES);

            // EmailDto 生成とメール送信
            EmailDto emailInfo = new EmailDto();
            emailInfo.setSendEmail(email);
            emailInfo.setVerificationCode(code);
            emailService.sendPasswordResetVerificationEmail(emailInfo);

            log.info(CM011003MessageConstant.EMAIL_SEND_SUCCESS_LOG, email, code);

            return ResponseEntity.ok(createResponseModel(
                    Map.of("message", CM011003MessageConstant.EMAIL_SEND_SUCCESS),
                    true,
                    CM011003MessageConstant.EMAIL_SEND_SUCCESS));

        } catch (Exception e) {
            log.error(CM011003MessageConstant.EMAIL_SEND_ERROR_LOG, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyMap(), false,
                            CM011003MessageConstant.EMAIL_SEND_FAILED));
        }
    }

    @Override
    public ResponseEntity<ResponseModel<Map<String, Object>>> verifyCodeForFindPw(String email, String code) {
        String key = PW_VERIFICATION_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            log.warn(CM011003MessageConstant.VERIFY_EXPIRED_LOG, email);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(Collections.emptyMap(), false,
                            CM011003MessageConstant.VERIFICATION_CODE_INVALID));
        }

        if (!storedCode.equals(code)) {
            log.warn(CM011003MessageConstant.VERIFY_MISMATCH_LOG, email, code, storedCode);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(Collections.emptyMap(), false,
                            CM011003MessageConstant.VERIFICATION_CODE_INVALID));
        }

        log.info(CM011003MessageConstant.VERIFY_SUCCESS_LOG, email);
        // 認証成功時にRedisキーは即時削除せず、最終的なパスワード変更時に削除
        return ResponseEntity.ok(createResponseModel(
                Map.of("verified", true),
                true,
                CM011003MessageConstant.VERIFICATION_SUCCESS));
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Map<String, String>>> resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail();
        String newPassword = request.getNewPassword();
        String key = PW_VERIFICATION_PREFIX + email;

        // 認証が完了しているか確認
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            log.warn(CM011003MessageConstant.VERIFICATION_REQUIRED_LOG, email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createResponseModel(Collections.emptyMap(), false,
                            CM011003MessageConstant.VERIFICATION_REQUIRED));
        }

        try {
            // ユーザー情報を取得
            User user = mapper.findByEmail(email);
            if (user == null) {
                log.error(CM011003MessageConstant.USER_NOT_FOUND_LOG, email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createResponseModel(Collections.emptyMap(), false,
                                CM011003MessageConstant.USER_NOT_FOUND));
            }

            // パスワード再利用検査
            List<PasswordHistory> history = passwordHistoryMapper.findByMemberNumber(user.getMemberNumber());
            for (PasswordHistory oldPassword : history) {
                if (passwordEncoder.matches(newPassword, oldPassword.getPassword())) {
                    // 過去6か月以内に使用したパスワードかどうかを確認
                    if (oldPassword.getCreatedAt().isAfter(LocalDateTime.now().minusMonths(6))) {
                        log.warn(CM011003MessageConstant.PASSWORD_REUSE_LOG, email);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(createResponseModel(Collections.emptyMap(), false,
                                        CM011003MessageConstant.PASSWORD_REUSE_WITHIN_6_MONTHS));
                    }
                }
            }

            // 現在のパスワードを履歴に追加
            PasswordHistory passwordHistory = new PasswordHistory();
            passwordHistory.setMemberNumber(user.getMemberNumber());
            passwordHistory.setPassword(user.getPassword());
            passwordHistory.setCreatedAt(LocalDateTime.now());
            passwordHistoryMapper.insertPasswordHistory(passwordHistory);
            log.info(CM011003MessageConstant.ADD_RESET_PASSWORD_HISTORY, user.getMemberNumber());

            // 新しいパスワードに更新
            user.setPassword(passwordEncoder.encode(newPassword));
            mapper.updatePassword(user);

            // 成功時にRedisキー削除
            redisTemplate.delete(key);
            log.info(CM011003MessageConstant.RESET_PASSWORD_SUCCESS_LOG, email);

            return ResponseEntity.ok(createResponseModel(
                    Collections.emptyMap(),
                    true,
                    CM011003MessageConstant.PASSWORD_RESET_SUCCESS));

        } catch (Exception e) {
            log.error(CM011003MessageConstant.RESET_PASSWORD_ERROR_UNEXPECTED, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyMap(), false,
                            CM011003MessageConstant.PASSWORD_RESET_FAILED));
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