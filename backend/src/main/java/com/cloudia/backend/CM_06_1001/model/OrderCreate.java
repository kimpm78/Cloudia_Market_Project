package com.cloudia.backend.CM_06_1001.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreate {    
    private Long userId;             // 注文者会員ID
    private String memberNumber;     // 注文者会員番号
    private List<Long> cartItemIds;  // カートから選択された項目
    private Integer shippingFee;     // 配送料
    private String shippingArea;     // 配送区分
    private String paymentMethod;    // 選択：決済手段
    private ShippingInfo shipping;   // 配送情報一式
    private Integer totalAmount;     // 合計金額
    private String createdBy;        // 登録者
    private LocalDateTime createdAt; // 登録日
    private String updatedBy;        // 更新者
    private LocalDateTime updatedAt; // 更新日
}