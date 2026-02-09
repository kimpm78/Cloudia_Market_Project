package com.cloudia.backend.CM_90_1000.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.cloudia.backend.CM_90_1000.model.Menu;

@Mapper
public interface CM901000Mapper {
    /*
     * サイドメニュー一覧を取得
     */
    List<Menu> findAllMenus();
}
