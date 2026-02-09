package com.cloudia.backend.common.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.cloudia.backend.common.model.CodeMaster;

@Mapper
public interface CodeMasterMapper {

    /**
     * 특정 이름을 가진 단일 코드를 조회
     */
    CodeMaster findByCodeTypeAndName(
            @Param("codeType") String codeType,
            @Param("codeValue") int codeValue);

    /**
     * 특정 codeType을 가진 코드 목록 전체를 조회
     */
    List<CodeMaster> findByCodeType(@Param("codeType") String codeType);
}