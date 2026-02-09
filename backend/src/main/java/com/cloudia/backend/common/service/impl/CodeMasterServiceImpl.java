package com.cloudia.backend.common.service.impl;

import com.cloudia.backend.common.mapper.CodeMasterMapper;
import com.cloudia.backend.common.model.CodeMaster;
import com.cloudia.backend.common.service.CodeMasterService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CodeMasterServiceImpl implements CodeMasterService {

    private final CodeMasterMapper codeMasterMapper;

    @Override
    public CodeMaster getCodeByValue(String codeType, int codeValue) {
        return codeMasterMapper.findByCodeTypeAndName(codeType, codeValue);
    }

    @Override
    public List<CodeMaster> getCodesByType(String codeType) {
        return codeMasterMapper.findByCodeType(codeType);
    }
}