package com.cloudia.backend.CM_90_1043.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_90_1043.model.CategorySaveRequest;

public interface CM901043Service {
    /**
     * カテゴリ全件一覧取得
     * 
     * @return カテゴリ全件一覧
     */
    ResponseEntity<ResponseModel<Map<String, Object>>> findByAllCategory();

    /**
     * カテゴリ変更内容の保存（追加/更新/削除）
     */
    ResponseEntity<ResponseModel<String>> saveChanges(CategorySaveRequest request, String userId);
}
