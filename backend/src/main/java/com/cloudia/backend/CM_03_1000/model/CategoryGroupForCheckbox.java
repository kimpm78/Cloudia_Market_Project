package com.cloudia.backend.CM_03_1000.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CategoryGroupForCheckbox {
    private String groupCode;              // カテゴリーグループコード
    private String groupName;              // カテゴリーグループ名
    private List<CategoryItem> categories; // カテゴリー項目一覧
}