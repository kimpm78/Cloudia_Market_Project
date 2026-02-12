package com.cloudia.backend.common.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.cloudia.backend.common.model.CodeMaster;

@Mapper
public interface CodeMasterMapper {

    /**
     * 特定の名称を持つ単一コードを取得
     */
    CodeMaster findByCodeTypeAndName(
            @Param("codeType") String codeType,
            @Param("codeValue") int codeValue);

    /**
     * 特定のcodeTypeを持つコード一覧を取得
     */
    List<CodeMaster> findByCodeType(@Param("codeType") String codeType);
}