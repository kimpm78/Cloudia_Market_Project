package com.cloudia.backend.CM_01_1007.service.impl;

import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_01_1007.constants.CM011007MessageConstant;
import com.cloudia.backend.CM_01_1007.mapper.CM011007Mapper;
import com.cloudia.backend.CM_01_1007.model.UserProfile;
import com.cloudia.backend.CM_01_1007.service.CM011007Service;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class CM011007ServiceImpl implements CM011007Service {

    private final CM011007Mapper mapper;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<UserProfile> getProfile(String loginId) {
        UserProfile profile = mapper.findProfileByLoginId(loginId);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    @Override
    public ResponseEntity<Map<String, Object>> updateProfile(String loginId, UserProfile userProfile) {
        User user = mapper.findUserByLoginId(loginId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", CM011007MessageConstant.FAIL_USER_NOT_FOUND));
        }

        user.setNationality(userProfile.getNationality());
        user.setPhoneNumber(userProfile.getPhoneNumber());
        user.setPostalCode(userProfile.getPostalCode());
        user.setAddressMain(userProfile.getAddressMain());
        user.setAddressDetail1(userProfile.getAddressDetail1());
        user.setAddressDetail2(userProfile.getAddressDetail2());
        user.setAddressDetail3(userProfile.getAddressDetail3());
        user.setPccc(userProfile.getPccc());
        user.setUpdatedBy(loginId);

        mapper.updateUserProfile(user);

        return ResponseEntity.ok(Map.of("message", CM011007MessageConstant.SUCCESS_UPDATE_PROFILE));
    }
}