package com.cloudia.backend.CM_03_1002.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Categories {
    private String categoryGroupCode;       // カテゴリーグループコード
    private String categoryGroupName;       // カテゴリーグループ名
    private int displayOrder;               // 表示順
    private String createdBy;               // 登録者
    private LocalDateTime createdAt;        // 登録日
    private String updatedBy;               // 更新者
    private LocalDateTime updatedAt;        // 更新日
    private List<CategoryDetails> details;  // 下位カテゴリー一覧
}
