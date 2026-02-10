package com.cloudia.backend.CM_90_1043.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
public class CategorySaveRequest {
    private List<CategoryItem> created; // 新規作成
    private List<CategoryItem> updated; // 更新
    private List<DeleteItem> deleted;   // 削除
    private LocalDateTime maxUpdatedAt; // 最新更新日時

    @Data
    public static class CategoryItem {
        private String id;              // 既存ID（更新時のみ）
        private String title;           // カテゴリ名
        private Integer order;          // 順序
        private String parentId;        // 親ID（nullの場合はグループ）
        private String newParentId;     // 新しい親ID（SET句用）
        private String type;            // "group" または "category"
    }

    @Data
    public static class DeleteItem {
        private String id;              // 削除対象ID
        private String parentId;        // 親ID（nullの場合はグループ）
        private String type;            // "group" または "category"
    }
}
