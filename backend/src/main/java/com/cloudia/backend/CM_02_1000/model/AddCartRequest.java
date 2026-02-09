package com.cloudia.backend.CM_02_1000.model;

import lombok.Getter;
import lombok.Setter;

/**
 * カートに商品を追加する際に使用するリクエストDTO
 */
@Getter
@Setter
public class AddCartRequest {
    private Long userId;
    private String productId;
    private int quantity;
}
