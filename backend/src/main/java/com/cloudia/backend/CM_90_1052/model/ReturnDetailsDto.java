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
    private int returnId;               // 返品ID
    private int quantity;               // 数量
    private int unitPrice;              // 単価   
    private String productCode;         // 商品コード
    private String createdBy;           // 作成者
    private LocalDateTime createdAt;    // 作成日時
    private String updatedBy;           // 更新者
    private LocalDateTime updatedAt;    // 更新日時
}