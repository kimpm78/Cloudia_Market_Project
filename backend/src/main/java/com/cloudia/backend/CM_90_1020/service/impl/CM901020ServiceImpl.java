package com.cloudia.backend.CM_90_1020.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_90_1020.mapper.CM901020Mapper;
import com.cloudia.backend.CM_90_1020.model.PasswordHistoryDto;
import com.cloudia.backend.CM_90_1020.model.UsersDto;
import com.cloudia.backend.CM_90_1020.service.CM901020Service;
import com.cloudia.backend.common.exception.AuthenticationException;
import com.cloudia.backend.common.exception.BusinessException;
import com.cloudia.backend.common.exception.ErrorCode;
import com.cloudia.backend.common.exception.InvalidRequestException;
import com.cloudia.backend.common.log.LogHelper;
import com.cloudia.backend.common.log.LogMessage;
import com.cloudia.backend.common.util.DateCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM901020ServiceImpl implements CM901020Service {
    private final CM901020Mapper cm901020Mapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final DateCalculator dateCalculator;

    /**
     * 유저 전체 리스트 조회
     * 
     * @return 유저 전체 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<UsersDto> findByAllUsers() {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "유저 목록" });
        List<UsersDto> responseUserList = cm901020Mapper.findByAllUsers();

        if (responseUserList == null) {
            responseUserList = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "유저 목록", String.valueOf(responseUserList.size()) });

        return responseUserList;
    }

    /**
     * 유저 조회
     * 
     * @param searchTerm 키워드
     * @param searchType 타입 (1:사원 번호, 2:ID)
     * @return 유저 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<UsersDto> getFindUsers(String searchTerm, int searchType) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            LogHelper.log(LogMessage.COMMON_SELECT_EMPTY, new String[] { "유저 목록" });
            throw new InvalidRequestException(ErrorCode.VALIDATION_SEARCH_TERM_EMPTY);
        }
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "유저 목록" });

        List<UsersDto> responseUserList = cm901020Mapper.findByUsers(searchTerm.trim(), searchType);

        if (responseUserList == null) {
            responseUserList = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "유저 목록", String.valueOf(responseUserList.size()) });

        return responseUserList;
    }

    /**
     * 특정 유저 조회
     * 
     * @param searchTerm 키워드
     * @return 유저 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public UsersDto getFindUser(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            LogHelper.log(LogMessage.COMMON_SELECT_EMPTY, new String[] { "유저 목록" });
            throw new InvalidRequestException(ErrorCode.VALIDATION_SEARCH_TERM_EMPTY);
        }
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "유저 목록" });

        UsersDto responseUserList = cm901020Mapper.findByUser(searchTerm.trim());

        if (responseUserList == null) {
            LogHelper.log(LogMessage.COMMON_SELECT_FAIL, new String[] { "유저 목록" });
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }

        return responseUserList;
    }

    /**
     * 유저 업데이트
     * 
     * @param userInfo 유저 정보
     * @return 성공 여부
     */
    @Override
    @Transactional
    public Integer postUserUpdate(UsersDto entity, String userId) {
        if (null == entity) {
            LogHelper.log(LogMessage.COMMON_UPDATE_EMPTY, new String[] { "유저 목록" });
            throw new InvalidRequestException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "유저 목록" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }

        if (null != entity.getPassword() && !entity.getPassword().isBlank()) {
            validatePassword(entity);
        }

        int updateCount = updateUserInfo(entity, userId);

        LogHelper.log(LogMessage.COMMON_UPDATE_SUCCESS, new String[] { "유저 목록", entity.getMemberNumber() });

        return updateCount;
    }

    /**
     * 유저 업데이트
     * 
     * @param entity 유저 정보
     * @return 업데이트 결과
     */
    private int updateUserInfo(UsersDto entity, String userId) {
        UsersDto userModel = new UsersDto();
        userModel.setMemberNumber(entity.getMemberNumber());
        userModel.setName(entity.getName());
        userModel.setGenderValue(entity.getGenderValue());
        userModel.setBirthDate(entity.getBirthDate());
        userModel.setNationality(entity.getNationality());
        userModel.setPhoneNumber(entity.getPhoneNumber());
        userModel.setUserStatusValue(entity.getUserStatusValue());
        userModel.setRoleId(entity.getRoleId());
        userModel.setNote(entity.getNote());
        userModel.setEmail(entity.getEmail());
        userModel.setPostalCode(entity.getPostalCode());
        userModel.setAddressMain(entity.getAddressMain());
        userModel.setAddressDetail1(entity.getAddressDetail1());
        userModel.setAddressDetail2(entity.getAddressDetail2());
        userModel.setAddressDetail3(entity.getAddressDetail3());
        userModel.setRefundAccountHolder(entity.getRefundAccountHolder());
        userModel.setRefundAccountNumber(entity.getRefundAccountNumber());
        userModel.setRefundAccountBank(entity.getRefundAccountBank());
        userModel.setPccc(entity.getPccc());
        userModel.setUpdatedBy(userId);
        userModel.setUpdatedAt(dateCalculator.tokyoTime());

        int updateCount = cm901020Mapper.userUpload(userModel);

        if (updateCount == 0) {
            LogHelper.log(LogMessage.COMMON_UPDATE_FAIL, new String[] { "유저 목록", entity.getMemberNumber() });
            throw new BusinessException(ErrorCode.UPDATE_FAILED);
        }

        if (null != entity.getPassword() && !entity.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(entity.getPassword());
            userModel.setPassword(encodedPassword);
            int historyCount = cm901020Mapper.insertPasswordHistory(userModel);
            if (historyCount == 0) {
                throw new BusinessException(ErrorCode.UPDATE_FAILED);
            }
        }

        return updateCount;
    }

    /**
     * 비밀번호 검증
     * 
     * @param entity 유저 정보
     */
    private void validatePassword(UsersDto entity) {

        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";

        if (!entity.getPassword().matches(passwordPattern)) {
            throw new InvalidRequestException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (isPasswordUsedInLast6Months(
                entity.getMemberNumber(),
                entity.getPassword())) {
            throw new InvalidRequestException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /**
     * 6개월 이내 비밀번호 히스토리 확인
     * 
     * @param memberNumber  사원 번호
     * @param plainPassword 비밀번호
     * @return 6개월 이내 히스토리 결과
     */
    public boolean isPasswordUsedInLast6Months(String memberNumber, String plainPassword) {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<PasswordHistoryDto> recentPasswords = cm901020Mapper.findRecentPasswordHistory(memberNumber,
                sixMonthsAgo);

        return recentPasswords.stream()
                .anyMatch(history -> passwordEncoder.matches(plainPassword, history.getPassword()));
    }
}
