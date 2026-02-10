package com.cloudia.backend.CM_03_1001.model;

import java.time.LocalDateTime;

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
public class CategoryDetails {
    private String categoryGroupCode;   // カテゴリーグループコード
    private String categoryCode;        // カテゴリーコード
    private String categoryName;        // カテゴリー名
    private int displayOrder;           // 表示順
    private String createdBy;           // 登録者
    private LocalDateTime createdAt;    // 登録日
    private String updatedBy;           // 更新者
    private LocalDateTime updatedAt;    // 更新日
}