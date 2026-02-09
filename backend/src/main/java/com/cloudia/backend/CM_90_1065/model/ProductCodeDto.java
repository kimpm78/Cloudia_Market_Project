package com.cloudia.backend.CM_90_1065.model;

import java.time.LocalDateTime;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
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
public class ProductCodeDto {
    private String productCode;         // 상품 코드
    private String productName;         // 상품명
    private String productCategory;     // 상품 분류
    private String createdBy;           // 등록자
    private LocalDateTime createdAt;    // 등록일
    private String updatedBy;           // 수정자
    private LocalDateTime updatedAt;    // 수정일
}
