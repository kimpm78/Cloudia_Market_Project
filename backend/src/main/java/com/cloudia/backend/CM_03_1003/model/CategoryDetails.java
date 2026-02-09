package com.cloudia.backend.CM_03_1003.model;

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
    private String categoryGroupCode; // 카테고리 그룹 코드
    private String categoryCode; // 카테고리 코드
    private String categoryName; // 카테고리 명
    private int displayOrder; // 표시 순서
    private String createdBy; // 등록자
    private LocalDateTime createdAt; // 등록일
    private String updatedBy; // 수정자
    private LocalDateTime updatedAt; // 수정일
}
