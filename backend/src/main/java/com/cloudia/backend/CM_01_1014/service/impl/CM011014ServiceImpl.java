package com.cloudia.backend.CM_01_1014.service.impl;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_01_1014.constants.CM011014MessageConstant;
import com.cloudia.backend.CM_01_1014.mapper.CM011014Mapper;
import com.cloudia.backend.CM_01_1014.model.UserAccount;
import com.cloudia.backend.CM_01_1014.service.CM011014Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CM011014ServiceImpl implements CM011014Service {

    private final CM011014Mapper mapper;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<UserAccount>> getAccount(String loginId) {
        UserAccount account = mapper.findAccountByLoginId(loginId);

        if (account == null) {
            account = new UserAccount();
        }

        // ResponseModel로 감싸기
        ResponseModel<UserAccount> response = ResponseModel.<UserAccount>builder()
                .result(true)
                .message("조회 성공")
                .resultList(account)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ResponseModel<Object>> updateAccount(String loginId, UserAccount accountDto) {
        User user = mapper.findUserByLoginId(loginId);

        // 1. 유저가 없는 경우 에러 응답
        if (user == null) {
            ResponseModel<Object> errorResponse = ResponseModel.builder()
                    .result(false)
                    .message("사용자를 찾을 수 없습니다.")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        user.setRefundAccountBank(accountDto.getRefundAccountBank());
        user.setRefundAccountNumber(accountDto.getRefundAccountNumber());
        user.setRefundAccountHolder(accountDto.getRefundAccountHolder());

        // 3. DB 업데이트
        mapper.updateUserAccount(user);

        // 4. 성공 응답 생성
        ResponseModel<Object> successResponse = ResponseModel.builder()
                .result(true)
                .message(CM011014MessageConstant.SUCCESS_UPDATE_ACCOUNT)
                .build();

        return ResponseEntity.ok(successResponse);
    }
}