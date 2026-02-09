package com.cloudia.backend.CM_01_1010.service.impl;

import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_01_1010.constants.CM011010MessageConstant;
import com.cloudia.backend.CM_01_1010.mapper.CM011010Mapper;
import com.cloudia.backend.CM_01_1010.model.Unsubscribe;
import com.cloudia.backend.CM_01_1010.service.CM011010Service;
import com.cloudia.backend.common.model.EmailDto;
import com.cloudia.backend.common.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CM011010ServiceImpl implements
        CM011010Service {

    private final CM011010Mapper cm011010Mapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public ResponseEntity<Map<String, Object>> unsubscribe(Unsubscribe request) {
        log.info(CM011010MessageConstant.SERVICE_START, request.getUserId());

        try {
            User user = cm011010Mapper.findByUserId(request.getUserId());
            if (user == null) {
                log.warn(CM011010MessageConstant.WARN_USER_NOT_FOUND, request.getUserId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", CM011010MessageConstant.FAIL_USER_NOT_FOUND));
            }

            int activeOrders = cm011010Mapper.countActiveOrders(user.getMemberNumber());
            if (activeOrders > 0) {
                log.warn(CM011010MessageConstant.WARN_ACTIVE_ORDERS_EXIST, activeOrders, request.getUserId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", CM011010MessageConstant.FAIL_ACTIVE_ORDERS_EXIST));
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn(CM011010MessageConstant.WARN_PASSWORD_MISMATCH, request.getUserId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", CM011010MessageConstant.FAIL_PASSWORD_MISMATCH));
            }

            String combinedReason = "";
            if (request.getReasons() != null && !request.getReasons().isEmpty()) {
                combinedReason = String.join(", ", request.getReasons());
            }

            // 사용자 계정을 비활성화
            cm011010Mapper.deactivateUser(request.getUserId(), combinedReason);
            log.info(CM011010MessageConstant.INFO_DEACTIVATED, request.getUserId());

            try {
                EmailDto emailDto = new EmailDto();
                emailDto.setSendEmail(user.getEmail());
                emailDto.setName(user.getName());
                emailDto.setLoginId(user.getLoginId());
                emailService.sendWithdrawalNotification(emailDto);
                log.info("회원 탈퇴 알림 이메일 발송 완료: {}", user.getEmail());
            } catch (Exception e) {
                log.warn("회원 탈퇴 이메일 발송 실패: {}", e.getMessage());
            }

            log.info(CM011010MessageConstant.UNSUBSCRIBE_END, request.getUserId());
            return ResponseEntity.ok(Map.of("message", CM011010MessageConstant.SUCCESS_UNSUBSCRIBE));

        } catch (Exception e) {
            log.error(CM011010MessageConstant.ERROR_UNEXPECTED, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", CM011010MessageConstant.FAIL_UNEXPECTED_ERROR));
        }
    }
}
