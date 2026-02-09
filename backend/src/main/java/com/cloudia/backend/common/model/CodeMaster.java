package com.cloudia.backend.common.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeMaster {
    private String codeType;
    private String codeTypeName;
    private int codeValue;
    private String codeValueName;
    private int sortOrder;
}