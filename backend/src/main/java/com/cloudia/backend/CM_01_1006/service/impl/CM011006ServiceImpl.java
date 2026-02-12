package com.cloudia.backend.CM_01_1006.service.impl;

import com.cloudia.backend.CM_01_1000.mapper.CM011000Mapper;
import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_01_1006.constants.CM011006MessageConstant;
import com.cloudia.backend.CM_01_1006.mapper.CM011006Mapper;
import com.cloudia.backend.CM_01_1006.model.*;
import com.cloudia.backend.CM_01_1006.service.CM011006Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CM011006ServiceImpl implements CM011006Service {

    private final CM011006Mapper inquiryMapper;
    private final CM011000Mapper userMapper;

    private String getMemberNumberByLoginId(String loginId) {
        User user = userMapper.findByLoginId(loginId);
        if (user == null) {
            throw new RuntimeException("ユーザー情報が見つかりません: " + loginId);
        }
        return user.getMemberNumber();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InquiryResponseDTO> getMyInquiries(String loginId) {
        log.info(CM011006MessageConstant.LOG_INQUIRY_LIST_START, loginId);

        try {
            String memberNumber = getMemberNumberByLoginId(loginId);
            if (memberNumber == null)
                return Collections.emptyList();

            return inquiryMapper.findByMemberNumber(memberNumber);
        } catch (Exception e) {
            log.error(CM011006MessageConstant.LOG_INQUIRY_LIST_ERROR, loginId, e);
            throw new RuntimeException(CM011006MessageConstant.MSG_SERVICE_ERROR, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<InquiryProductDTO> getProductList() {
        return inquiryMapper.getProductList();
    }

    @Override
    @Transactional
    public InquiryResponseDTO createInquiry(String loginId, InquiryWriteDTO writeDTO) {
        log.info(CM011006MessageConstant.LOG_INQUIRY_CREATE_START, loginId);

        String memberNumber = getMemberNumberByLoginId(loginId);

        InquiryEntity entity = InquiryEntity.builder()
                .memberNumber(memberNumber)
                .title(writeDTO.getTitle())
                .content(writeDTO.getContent())
                .createdBy(loginId)
                .createdAt(LocalDateTime.now())
                .isPrivate(writeDTO.getIsPrivate())
                .inquiryStatusType("006")
                .inquiryStatusValue(1)
                .inquiriesCodeType("012")
                .build();

        try {
            entity.setInquiriesCodeValue(Integer.parseInt(writeDTO.getCategoryCode()));
        } catch (NumberFormatException | NullPointerException e) {
            entity.setInquiriesCodeValue(3);
        }

        inquiryMapper.insertInquiry(entity);

        return inquiryMapper.findDetailByInquiryId(entity.getInquiryId());
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<InquiryDetailResponse> getInquiryDetail(Long inquiryId, String loginId) {

        try {
            InquiryResponseDTO current = inquiryMapper.findDetailByInquiryId(inquiryId);
            if (current == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // 権限チェックのため User 情報を取得
            User user = userMapper.findByLoginId(loginId);
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            String requesterMemberNumber = user.getMemberNumber();
            boolean isAdmin = hasAdminAuthority(user);

            boolean isPrivate = current.getIsPrivate() != null && current.getIsPrivate() == 1;

            if (isPrivate && !isAdmin && !Objects.equals(requesterMemberNumber, current.getMemberNumber())) {
                log.warn(CM011006MessageConstant.LOG_INQUIRY_DETAIL_FORBIDDEN, inquiryId, requesterMemberNumber);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            InquiryResponseDTO prev = inquiryMapper.findPrevInquiry(inquiryId, requesterMemberNumber);
            InquiryResponseDTO next = inquiryMapper.findNextInquiry(inquiryId, requesterMemberNumber);

            return ResponseEntity.ok(new InquiryDetailResponse(current, prev, next));

        } catch (Exception e) {
            log.error(CM011006MessageConstant.LOG_INQUIRY_DETAIL_ERROR, inquiryId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Void> deleteInquiry(Long inquiryId, String loginId) {

        String requesterMemberNumber = getMemberNumberByLoginId(loginId);
        log.info(CM011006MessageConstant.LOG_DELETE_START, inquiryId, requesterMemberNumber);

        try {
            InquiryResponseDTO inquiry = inquiryMapper.findDetailByInquiryId(inquiryId);
            if (inquiry == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            if (!Objects.equals(requesterMemberNumber, inquiry.getMemberNumber())) {
                log.warn(CM011006MessageConstant.LOG_DELETE_FORBIDDEN, inquiryId, requesterMemberNumber);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (inquiry.getStatusValue() != null && inquiry.getStatusValue() == 2) {
                log.warn(CM011006MessageConstant.LOG_DELETE_FAIL_ANSWERED, inquiryId, inquiry.getStatusValue());
                return ResponseEntity.badRequest()
                        .header("X-Error-Message", CM011006MessageConstant.MSG_DELETE_FAIL_ANSWERED)
                        .build();
            }

            int deletedRows = inquiryMapper.deleteInquiryById(inquiryId);

            if (deletedRows > 0) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

        } catch (DataAccessException dae) {
            log.error(CM011006MessageConstant.LOG_DELETE_ERROR, inquiryId, dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error(CM011006MessageConstant.LOG_DELETE_UNKNOWN_ERROR, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean hasAdminAuthority(User user) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") ||
                        auth.getAuthority().equals("ROLE_MANAGER"));
    }
}