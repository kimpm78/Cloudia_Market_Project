package com.cloudia.backend.CM_03_1000.model;

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
    private String categoryGroupCode; // 카테고리 그룹 코드
    private String categoryGroupName; // 카테고리 그룹 명
    private int displayOrder; // 표시 순서
    private String createdBy; // 등록자
    private LocalDateTime createdAt; // 등록일
    private String updatedBy; // 수정자
    private LocalDateTime updatedAt; // 수정일
    private List<CategoryDetails> details; // 하위 카테고리 리스트
}
