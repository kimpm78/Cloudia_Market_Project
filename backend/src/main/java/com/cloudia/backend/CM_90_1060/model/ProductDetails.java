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
public class ProductDetails {
    private Long productId;             // 商品ID
    private String thumbnailUrl;        // 商品サムネイルURL
    private String description;         // 商品説明
    private String createdBy;           // 登録者
    private LocalDateTime createdAt;    // 登録日
    private String updatedBy;           // 更新者
    private LocalDateTime updatedAt;    // 更新日
}
