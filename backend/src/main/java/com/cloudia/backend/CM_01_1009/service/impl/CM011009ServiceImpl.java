package com.cloudia.backend.CM_01_1009.service.impl;

import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_01_1009.constants.CM011009MessageConstant;
import com.cloudia.backend.CM_01_1009.mapper.CM011009Mapper;
import com.cloudia.backend.CM_01_1009.model.ChangePassword;
import com.cloudia.backend.CM_01_1009.service.CM011009Service;
import com.cloudia.backend.common.model.EmailDto;
import com.cloudia.backend.common.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CM011009ServiceImpl implements CM011009Service {

    private final CM011009Mapper CM011009Mapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public ResponseEntity<Map<String, Object>> changePassword(String loginId, ChangePassword request) {
        log.info(com.cloudia.backend.CM_01_1009.constants.CM011009MessageConstant.SERVICE_START, loginId);

        try {
            if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", CM011009MessageConstant.FAIL_PASSWORD_CONFIRM_MISMATCH));
            }

            User user = CM011009Mapper.findByLoginId(loginId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", CM011009MessageConstant.FAIL_USER_NOT_FOUND));
            }

            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", CM011009MessageConstant.FAIL_CURRENT_PASSWORD_MISMATCH));
            }

            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", CM011009MessageConstant.FAIL_NEW_PASSWORD_SAME_AS_OLD));
            }

            if (isPasswordUsedInLast6Months(user.getMemberNumber(), request.getNewPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", CM011009MessageConstant.FAIL_PASSWORD_USED_LAST_6_MONTHS));
            }

            String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
            CM011009Mapper.updatePassword(user.getUserId(), encodedNewPassword);
            CM011009Mapper.insertPasswordHistory(user.getMemberNumber(), encodedNewPassword);

            log.info(CM011009MessageConstant.SERVICE_END, loginId);
            try {
                EmailDto emailDto = new EmailDto();
                emailDto.setSendEmail(user.getEmail());
                emailDto.setName(user.getName());
                emailService.sendPasswordChangedNotification(emailDto);
                log.info("パスワード変更通知メール送信完了: {}", user.getEmail());
            } catch (Exception e) {
                log.warn("メール送信失敗（パスワード変更は完了）: {}", e.getMessage());
            }

            log.info(CM011009MessageConstant.SERVICE_END, loginId);

            return ResponseEntity.ok(Map.of("message", CM011009MessageConstant.SUCCESS_CHANGE_PASSWORD));

        } catch (Exception e) {
            log.error(CM011009MessageConstant.UNEXPECTED_ERROR, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", CM011009MessageConstant.FAIL_UNEXPECTED_ERROR));
        }
    }

    /**
     * 過去6か月以内のパスワード使用有無を確認
     *
     * @param memberNumber 会員番号（DBの password_history テーブルキー）
     */
    private boolean isPasswordUsedInLast6Months(String memberNumber, String plainPassword) {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);

        // memberNumber と日付で取得
        List<String> recentPasswordHashes = CM011009Mapper.findRecentPasswords(memberNumber, sixMonthsAgo);

        // 比較
        return recentPasswordHashes.stream()
                .anyMatch(historyHash -> passwordEncoder.matches(plainPassword, historyHash));
    }
}