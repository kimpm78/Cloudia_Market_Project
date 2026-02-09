package com.cloudia.backend.CM_01_1008.service.impl;

import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_01_1008.constants.CM011008MessageConstant;
import com.cloudia.backend.CM_01_1008.mapper.CM011008Mapper;
import com.cloudia.backend.CM_01_1008.model.DeliveryAddress;
import com.cloudia.backend.CM_01_1008.service.CM011008Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CM011008ServiceImpl implements CM011008Service {

    private final CM011008Mapper cm011008Mapper;
    private static final int MAX_ADDRESS_COUNT = 3;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<DeliveryAddress>> getAddressesByUsername(String username) {
        log.info(CM011008MessageConstant.GET_ADDRESSES_START, username);
        User user = cm011008Mapper.findUserByLoginId(username);
        if (user == null) {
            log.warn(CM011008MessageConstant.FAIL_USER_NOT_FOUND + " username: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        List<DeliveryAddress> addresses = cm011008Mapper.findAddressesByMemberNumber(user.getMemberNumber());
        log.info(CM011008MessageConstant.GET_ADDRESSES_END, username);
        return ResponseEntity.ok(addresses);
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> addAddress(String username, DeliveryAddress address) {
        log.info(CM011008MessageConstant.ADD_ADDRESS_START, username);
        User user = cm011008Mapper.findUserByLoginId(username);
        if (user == null) {
            return createErrorResponse(HttpStatus.NOT_FOUND, CM011008MessageConstant.FAIL_USER_NOT_FOUND);
        }

        int currentAddressCount = cm011008Mapper.countAddressesByMemberNumber(user.getMemberNumber());
        if (currentAddressCount >= MAX_ADDRESS_COUNT) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, CM011008MessageConstant.FAIL_ADDRESS_LIMIT_EXCEEDED);
        }

        boolean requestDefault = Boolean.TRUE.equals(address.getIsDefault()) || currentAddressCount == 0;
        if (requestDefault) {
            cm011008Mapper.resetDefaultAddress(user.getMemberNumber());
            address.setIsDefault(Boolean.TRUE);
        } else {
            address.setIsDefault(Boolean.FALSE);
        }
        address.setMemberNumber(user.getMemberNumber());
        address.setCreatedBy(username);
        address.setUpdatedBy(username);
        cm011008Mapper.insertAddress(address);
        log.info(CM011008MessageConstant.ADD_ADDRESS_END, username);
        return createSuccessResponse(CM011008MessageConstant.SUCCESS_ADD_ADDRESS);
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> updateAddress(String username, Integer addressId,
            DeliveryAddress address) {
        log.info(CM011008MessageConstant.UPDATE_ADDRESS_START, addressId);
        User user = cm011008Mapper.findUserByLoginId(username);
        if (user == null) {
            return createErrorResponse(HttpStatus.NOT_FOUND, CM011008MessageConstant.FAIL_USER_NOT_FOUND);
        }

        DeliveryAddress existing = cm011008Mapper.findAddressById(addressId)
                .orElse(null);
        if (existing == null) {
            return createErrorResponse(HttpStatus.NOT_FOUND, CM011008MessageConstant.FAIL_ADDRESS_NOT_FOUND);
        }
        if (!user.getMemberNumber().equals(existing.getMemberNumber())) {
            return createErrorResponse(HttpStatus.FORBIDDEN, CM011008MessageConstant.FAIL_ADDRESS_FORBIDDEN);
        }

        boolean requestDefault = Boolean.TRUE.equals(address.getIsDefault());
        if (requestDefault) {
            cm011008Mapper.resetDefaultAddress(existing.getMemberNumber());
        }

        DeliveryAddress merged = mergeAddress(existing, address);
        merged.setAddressId(addressId);
        merged.setMemberNumber(existing.getMemberNumber());
        merged.setUpdatedBy(username);
        Boolean resolvedDefault;
        if (requestDefault) {
            resolvedDefault = Boolean.TRUE;
        } else if (address.getIsDefault() == null) {
            resolvedDefault = existing.getIsDefault();
        } else {
            resolvedDefault = Boolean.FALSE;
        }
        merged.setIsDefault(resolvedDefault);

        cm011008Mapper.updateAddress(merged);
        log.info(CM011008MessageConstant.UPDATE_ADDRESS_END, addressId);
        return createSuccessResponse(CM011008MessageConstant.SUCCESS_UPDATE_ADDRESS);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<DeliveryAddress> getAddressById(Integer addressId) {
        return cm011008Mapper.findAddressById(addressId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @Transactional
    public ResponseEntity<Void> deleteAddress(Integer addressId) {
        log.info(CM011008MessageConstant.DELETE_ADDRESS_START, addressId);
        cm011008Mapper.deleteAddressSoft(addressId); // Soft delete
        log.info(CM011008MessageConstant.DELETE_ADDRESS_END, addressId);
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<Map<String, Object>> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }

    private DeliveryAddress mergeAddress(DeliveryAddress existing, DeliveryAddress incoming) {
        DeliveryAddress merged = new DeliveryAddress();
        merged.setAddressNickname(coalesceMandatory(incoming.getAddressNickname(), existing.getAddressNickname()));
        merged.setRecipientName(coalesceMandatory(incoming.getRecipientName(), existing.getRecipientName()));
        merged.setRecipientPhone(coalesceMandatory(incoming.getRecipientPhone(), existing.getRecipientPhone()));
        merged.setPostalCode(coalesceMandatory(incoming.getPostalCode(), existing.getPostalCode()));
        merged.setAddressMain(coalesceMandatory(incoming.getAddressMain(), existing.getAddressMain()));
        merged.setAddressDetail1(coalesceMandatory(incoming.getAddressDetail1(), existing.getAddressDetail1()));
        merged.setAddressDetail2(coalesceOptional(incoming.getAddressDetail2(), existing.getAddressDetail2()));
        merged.setAddressDetail3(coalesceOptional(incoming.getAddressDetail3(), existing.getAddressDetail3()));
        return merged;
    }

    @Override
    @Transactional
    public ResponseEntity<Void> setDefaultAddress(String username, Integer addressId) {
        User user = cm011008Mapper.findUserByLoginId(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        DeliveryAddress existing = cm011008Mapper.findAddressById(addressId).orElse(null);
        if (existing == null || !existing.getMemberNumber().equals(user.getMemberNumber())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        cm011008Mapper.resetDefaultAddress(user.getMemberNumber());
        cm011008Mapper.updateAddressDefaultStatus(addressId, true, username);

        return ResponseEntity.ok().build();
    }

    /**
     * 필수값 처리용 (Mandatory)
     */
    private String coalesceMandatory(String primary, String fallback) {
        if (primary != null) {
            String trimmed = primary.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return fallback;
    }

    /**
     * 선택값 처리용 (Optional)
     */
    private String coalesceOptional(String primary, String fallback) {
        if (primary != null) {
            return primary.trim();
        }
        return fallback;
    }
}
