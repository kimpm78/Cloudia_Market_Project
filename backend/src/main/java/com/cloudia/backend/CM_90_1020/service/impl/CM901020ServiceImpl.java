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
     * ユーザー全件一覧取得
     * 
     * @return ユーザー全件一覧
     */
    @Override
    @Transactional(readOnly = true)
    public List<UsersDto> findByAllUsers() {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "ユーザー一覧" });
        List<UsersDto> responseUserList = cm901020Mapper.findByAllUsers();

        if (responseUserList == null) {
            responseUserList = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "ユーザー一覧", String.valueOf(responseUserList.size()) });

        return responseUserList;
    }

    /**
     * ユーザー検索
     * 
     * @param searchTerm キーワード
     * @param searchType 種別 (1:社員番号, 2:ID)
     * @return ユーザー一覧
     */
    @Override
    @Transactional(readOnly = true)
    public List<UsersDto> getFindUsers(String searchTerm, int searchType) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            LogHelper.log(LogMessage.COMMON_SELECT_EMPTY, new String[] { "ユーザー一覧" });
            throw new InvalidRequestException(ErrorCode.VALIDATION_SEARCH_TERM_EMPTY);
        }
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "ユーザー一覧" });

        List<UsersDto> responseUserList = cm901020Mapper.findByUsers(searchTerm.trim(), searchType);

        if (responseUserList == null) {
            responseUserList = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "ユーザー一覧", String.valueOf(responseUserList.size()) });

        return responseUserList;
    }

    /**
     * 特定ユーザー取得
     * 
     * @param searchTerm キーワード
     * @return ユーザー情報
     */
    @Override
    @Transactional(readOnly = true)
    public UsersDto getFindUser(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            LogHelper.log(LogMessage.COMMON_SELECT_EMPTY, new String[] { "ユーザー一覧" });
            throw new InvalidRequestException(ErrorCode.VALIDATION_SEARCH_TERM_EMPTY);
        }
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "ユーザー一覧" });

        UsersDto responseUserList = cm901020Mapper.findByUser(searchTerm.trim());

        if (responseUserList == null) {
            LogHelper.log(LogMessage.COMMON_SELECT_FAIL, new String[] { "ユーザー一覧" });
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }

        return responseUserList;
    }

    /**
     * ユーザー更新
     * 
     * @param entity ユーザー情報
     * @return 更新件数
     */
    @Override
    @Transactional
    public Integer postUserUpdate(UsersDto entity, String userId) {
        if (null == entity) {
            LogHelper.log(LogMessage.COMMON_UPDATE_EMPTY, new String[] { "ユーザー一覧" });
            throw new InvalidRequestException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "ユーザー一覧" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }

        if (null != entity.getPassword() && !entity.getPassword().isBlank()) {
            validatePassword(entity);
        }

        int updateCount = updateUserInfo(entity, userId);

        LogHelper.log(LogMessage.COMMON_UPDATE_SUCCESS, new String[] { "ユーザー一覧", entity.getMemberNumber() });

        return updateCount;
    }

    /**
     * ユーザー更新
     * 
     * @param entity ユーザー情報
     * @return 更新結果
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
            LogHelper.log(LogMessage.COMMON_UPDATE_FAIL, new String[] { "ユーザー一覧", entity.getMemberNumber() });
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
     * パスワード検証
     * 
     * @param entity ユーザー情報
     */
    private void validatePassword(UsersDto entity) {

        // パスワード検証
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
     * 直近6か月のパスワード履歴確認
     * 
     * @param memberNumber  社員番号
     * @param plainPassword パスワード
     * @return 直近6か月以内の履歴有無
     */
    public boolean isPasswordUsedInLast6Months(String memberNumber, String plainPassword) {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<PasswordHistoryDto> recentPasswords = cm901020Mapper.findRecentPasswordHistory(memberNumber,
                sixMonthsAgo);

        return recentPasswords.stream()
                .anyMatch(history -> passwordEncoder.matches(plainPassword, history.getPassword()));
    }
}
