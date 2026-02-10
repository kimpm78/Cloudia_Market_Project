package com.cloudia.backend.CM_90_1043.model;

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
public class CategoryGroupDTO {
    private String categoryGroupCode;   // グループコード（PK）
    private String categoryGroupName;   // グループ名
    private Integer displayOrder;       // 表示順
    private String createdBy;           // 作成者
    private LocalDateTime createdAt;    // 作成日時
    private String updatedBy;           // 更新者
    private LocalDateTime updatedAt;    // 更新日時
}