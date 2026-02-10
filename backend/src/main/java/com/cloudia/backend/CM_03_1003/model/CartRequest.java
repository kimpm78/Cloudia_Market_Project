package com.cloudia.backend.CM_03_1003.model;

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
public class CartRequest {
    private Long userId;        // 使用者ID
    private String productId;   // 商品ID
    private Integer quantity;   // 商品をカートに追加する数量
}
