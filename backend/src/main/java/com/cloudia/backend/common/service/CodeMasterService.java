package com.cloudia.backend.common.service;

import java.util.List;

import com.cloudia.backend.common.model.CodeMaster;

public interface CodeMasterService {

    /**
     * 特定のコード値を持つ単一コードを取得
     *
     * @param codeType  コードタイプ
     * @param codeValue コード値
     * @return CodeMasterオブジェクト
     */
    CodeMaster getCodeByValue(String codeType, int codeValue);

    /**
     * 特定のコードタイプを持つコード一覧を取得
     *
     * @param codeType コードタイプ
     * @return CodeMasterオブジェクトのリスト
     */
    List<CodeMaster> getCodesByType(String codeType);

}