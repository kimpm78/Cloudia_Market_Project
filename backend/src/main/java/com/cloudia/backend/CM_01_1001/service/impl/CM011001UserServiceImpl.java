package com.cloudia.backend.CM_01_1001.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_01_1001.constants.CM011001MessageConstant;
import com.cloudia.backend.CM_01_1001.mapper.CM011001UserMapper;
import com.cloudia.backend.CM_01_1001.model.Address;
import com.cloudia.backend.CM_01_1001.model.SignUpRequestModel;
import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_01_1001.service.CM011001UserService;
import com.cloudia.backend.CM_01_1001.service.CM011001VerificationService;
import com.cloudia.backend.auth.constants.RoleType;
import com.cloudia.backend.auth.mapper.PasswordHistoryMapper;
import com.cloudia.backend.auth.mapper.RoleMapper;
import com.cloudia.backend.auth.model.PasswordHistory;
import com.cloudia.backend.auth.model.Role;
import com.cloudia.backend.common.model.CodeMaster;
import com.cloudia.backend.common.service.CodeMasterService;
import com.cloudia.backend.common.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CM011001UserServiceImpl implements CM011001UserService {

    private final CM011001UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CM011001VerificationService verificationService;
    private final PasswordHistoryMapper passwordHistoryMapper;
    private final CodeMasterService codeMasterService;
    private final RoleMapper roleMapper;
    private final EmailService emailService;

    @Value("${app.homepage.url}")
    private String appHomepageUrl;

    @Override
    public ResponseEntity<Map<String, Object>> signUp(SignUpRequestModel request) {

        log.info(CM011001MessageConstant.SIGNUP_SERVICE_START, request.getUser().getLoginId());

        ZoneId japanZone = ZoneId.of("Asia/Tokyo");
        LocalDateTime now = LocalDateTime.now(japanZone);

        try {
            User user = request.getUser();
            Address address = request.getAddress();

            // 유효성 검사 (ID, 이메일 중복 등)
            if (userMapper.countByLoginId(user.getLoginId()) > 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", CM011001MessageConstant.FAIL_ID_CONFLICT));
            }

            if (userMapper.countByEmail(user.getEmail()) > 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", CM011001MessageConstant.FAIL_EMAIL_CONFLICT));
            }

            if (!user.getPassword().equals(user.getPasswordConfirm())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", CM011001MessageConstant.FAIL_PASSWORD_MISMATCH));
            }

            if (!verificationService.isEmailVerified(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", CM011001MessageConstant.FAIL_VERIFICATION_EXPIRED, "errorCode",
                                "VERIFICATION_EXPIRED"));
            }

            if ("KR".equals(user.getNationality())) {
                String pccc = user.getPccc();
                pccc = pccc.trim().toUpperCase();
                if (userMapper.countByPccc(pccc) > 0) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "이미 등록된 개인통관고유부호입니다."));
                }
                user.setPccc(pccc);
            } else {
                user.setPccc(null);
            }

            // 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);

            // 주소 정보를 User 객체에 설정
            user.setPostalCode(address.getPostalCode());
            user.setAddressMain(address.getAddressMain());
            user.setAddressDetail1(address.getAddressDetail1());
            user.setAddressDetail2(address.getAddressDetail2());
            user.setAddressDetail3(address.getAddressDetail3());

            // 코드 마스터 및 역할(Role) 정보 설정
            CodeMaster genderCode = codeMasterService.getCodeByValue("010", user.getGenderValue());
            user.setGenderType(genderCode.getCodeType());
            user.setGenderValue(genderCode.getCodeValue());

            CodeMaster userStatusCode = codeMasterService.getCodeByValue("004", 1);
            user.setUserStatusType(userStatusCode.getCodeType());
            user.setUserStatusValue(userStatusCode.getCodeValue());

            Role userRole = roleMapper.findByRoleType(RoleType.USER);
            user.setRoleId(userRole.getRoleId());

            String loginId = user.getLoginId();
            user.setCreatedBy(loginId);
            user.setCreatedAt(now);
            user.setUpdatedBy(loginId);
            user.setUpdatedAt(now);

            // users 테이블에 사용자 정보 저장
            String memberNumber = userMapper.getNextMemberNumber();
            user.setMemberNumber(memberNumber);
            userMapper.insertUser(user);

            // DB에서 방금 생성된 User 정보를 다시 가져와 member_number 확인
            if (user.getUserId() == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", CM011001MessageConstant.FAIL_KEY_GENERATION));
            }

            User createdUser = userMapper.findByUserId(user.getUserId());
            if (createdUser == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", CM011001MessageConstant.FAIL_USER_NOT_FOUND));
            }

            // 비밀번호 이력 저장
            PasswordHistory passwordHistory = new PasswordHistory();
            passwordHistory.setMemberNumber(createdUser.getMemberNumber());
            passwordHistory.setPassword(encodedPassword);
            passwordHistory.setCreatedAt(now);
            passwordHistoryMapper.insertPasswordHistory(passwordHistory);

            verificationService.clearVerificationStatus(createdUser.getEmail());

            try {
                String joinDate = createdUser.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                String maskedLoginId = loginId;
                if (loginId.length() > 3) {
                    maskedLoginId = loginId.substring(0, loginId.length() - 3) + "***";
                }

                Map<String, String> templateData = Map.of(
                        "userName", createdUser.getName(),
                        "loginId", maskedLoginId,
                        "joinDate", joinDate,
                        "homePageUrl", appHomepageUrl);

                emailService.sendTemplateEmail(
                        "WelcomeEmail_kr",
                        createdUser.getEmail(),
                        templateData);

                log.info(CM011001MessageConstant.WELCOME_EMAIL_SEND_SUCCESS, createdUser.getEmail());

            } catch (Exception e) {
                log.error(CM011001MessageConstant.WELCOME_EMAIL_SEND_FAIL, createdUser.getEmail(), e);
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", CM011001MessageConstant.SUCCESS_SIGNUP, "userId", user.getUserId()));

        } catch (DataAccessException e) {
            log.error(CM011001MessageConstant.SIGNUP_ERROR_DB, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", CM011001MessageConstant.FAIL_DB_ERROR));

        } catch (NullPointerException e) {
            log.error(CM011001MessageConstant.SIGNUP_ERROR_NULL, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", CM011001MessageConstant.FAIL_NULL_REQUEST));

        } catch (Exception e) {
            log.error(CM011001MessageConstant.SIGNUP_ERROR_UNEXPECTED, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", CM011001MessageConstant.FAIL_UNEXPECTED_ERROR));
        }
    }

    @Override
    public ResponseEntity<Integer> checkLoginId(String loginId) {
        int count = userMapper.countByLoginId(loginId);
        return ResponseEntity.ok(count);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return userMapper.countByEmail(email) == 0;
    }

    @Override
    public ResponseEntity<Integer> checkPccc(String pccc) {
        if (pccc == null || pccc.trim().isEmpty()) {
            return ResponseEntity.ok(0);
        }
        int count = userMapper.countByPccc(pccc.trim().toUpperCase());
        return ResponseEntity.ok(count);
    }
}