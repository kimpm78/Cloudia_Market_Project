package com.cloudia.backend.CM_90_1052.model;

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
public class ReturnDetailsDto {
    private int returnId;
    private int quantity;
    private int unitPrice;
    private String productCode;
    private String createdBy; // 생성자
    private LocalDateTime createdAt; // 생성일시
    private String updatedBy; // 수정자
    private LocalDateTime updatedAt; // 수정일시
}
