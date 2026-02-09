package com.cloudia.backend.CM_01_1000.service.impl;

import com.cloudia.backend.CM_01_1000.constants.CM011000MessageConstant;
import com.cloudia.backend.CM_01_1000.mapper.CM011000Mapper;
import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.auth.mapper.RoleMapper;
import com.cloudia.backend.auth.model.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CM011000ServiceImpl implements UserDetailsService {

    private final CM011000Mapper loginMapper;
    private final RoleMapper roleMapper;

    @Override
    // Spring Securityがユーザー認証時にDBからユーザー情報を取得するために使用するサービス
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        User user = loginMapper.findByLoginId(loginId);
        if (user == null) {
            throw new UsernameNotFoundException(CM011000MessageConstant.USER_NOT_FOUND_FOR_ID + loginId);
        }

        final int STATUS_DORMANT = 2; // 休眠
        final int STATUS_INACTIVE = 3; // 退会/利用不可

        if (user.getUserStatusValue() != null) {
            if (user.getUserStatusValue() == STATUS_DORMANT) {
                throw new DisabledException("DORMANT_ACCOUNT:" + CM011000MessageConstant.DORMANT_ACCOUNT_MESSAGE);
            } else if (user.getUserStatusValue() == STATUS_INACTIVE) {
                throw new DisabledException("INACTIVE_ACCOUNT:" + CM011000MessageConstant.INACTIVE_ACCOUNT_MESSAGE);
            }
        }

        List<GrantedAuthority> authorities = getAuthorities(user.getRoleId());

        user.setAuthorities(authorities);

        log.info("読み込まれたユーザー: {}, 権限: {}", loginId, authorities);

        return user;
    }

    private List<GrantedAuthority> getAuthorities(int roleId) {
        Role role = roleMapper.findById(roleId);

        String roleName;

        if (role != null && role.getRoleType() != null) {
            roleName = role.getRoleType();
        } else {
            log.warn("無効なRole IDです: {}。ROLE_GUESTに置き換えます。", roleId);
            roleName = "ROLE_GUEST";
        }

        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }
}