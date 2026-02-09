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
    private List<CategoryItem> created; // 신규 생성
    private List<CategoryItem> updated; // 수정
    private List<DeleteItem> deleted; // 삭제
    private LocalDateTime maxUpdatedAt; // 최신 업데이트날짜

    @Data
    public static class CategoryItem {
        private String id; // 기존 ID (수정 시에만)
        private String title; // 카테고리명
        private Integer order; // 순서
        private String parentId; // 부모 ID (null이면 그룹)
        private String newParentId; // 새로운 부모 ID (SET 절용)
        private String type; // "group" 또는 "category"
    }

    @Data
    public static class DeleteItem {
        private String id; // 삭제할 ID
        private String parentId; // 부모 ID (null이면 그룹)
        private String type; // "group" 또는 "category"
    }
}
