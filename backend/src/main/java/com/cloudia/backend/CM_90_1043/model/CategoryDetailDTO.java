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
public class CategoryDetailDTO {
    private String categoryGroupCode; // 그룹 코드
    private String categoryCode; // 카테고리 코드
    private String categoryName; // 카테고리명
    private Integer displayOrder; // 표시 순서
    private Integer isActive; // 삭제 플래그
    private String createdBy; // 생성자
    private LocalDateTime createdAt; // 생성일시
    private String updatedBy; // 수정자
    private LocalDateTime updatedAt; // 수정일시
}
