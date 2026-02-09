package com.cloudia.backend.CM_01_1004.mapper;

import com.cloudia.backend.CM_90_1000.model.Menu;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CM011004Mapper {
    List<Menu> findMyPageMenus();
}