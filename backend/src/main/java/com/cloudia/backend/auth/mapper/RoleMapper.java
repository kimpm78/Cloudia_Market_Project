package com.cloudia.backend.auth.mapper;

import com.cloudia.backend.auth.model.Role;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper {
    List<Role> findAll();

    Role findByRoleType(String roleType);

    Role findById(int roleId);
}