package com.cloudia.backend.auth.service;

import com.cloudia.backend.auth.model.Role;
import com.cloudia.backend.auth.mapper.RoleMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;

    public List<Role> getAllRoles() {
        return roleMapper.findAll();
    }
}