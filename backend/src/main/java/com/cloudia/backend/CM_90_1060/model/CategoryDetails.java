package com.cloudia.backend.CM_90_1060.model;

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
    private String categoryGroupCode;   // カテゴリグループコード
    private String categoryCode;        // カテゴリコード
    private String categoryName;        // カテゴリ名
    private int displayOrder;           // 表示順
    private String createdBy;           // 登録者
    private LocalDateTime createdAt;    // 登録日
    private String updatedBy;           // 更新者
    private LocalDateTime updatedAt;    // 更新日
}
