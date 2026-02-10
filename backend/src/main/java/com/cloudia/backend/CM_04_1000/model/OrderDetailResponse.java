package com.cloudia.backend.CM_04_1000.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * レビュー作成時の注文＋商品情報DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailResponse {

    private Long orderId;
    private String memberNumber;                    // 注文者識別
    private String orderNumber;                     // 注文番号（例: '00001'）
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    private String productCode;                     // 注文詳細コード
    private Long productId;                         // products のPK
    private String productName;

    @Builder.Default
    private List<ReviewProduct> products = new ArrayList<>();
}