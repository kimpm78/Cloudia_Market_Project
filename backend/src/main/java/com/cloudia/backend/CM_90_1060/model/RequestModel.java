package com.cloudia.backend.CM_90_1060.model;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

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
public class RequestModel {
    private long productId;
    private String categoryGroup;           // カテゴリー
    private String productCode;             // 商品コード
    private String productName;             // 商品名
    private int productPrice;               // 商品価格
    private int shippingFee;                // 配送料
    private String purchasePrice;           // 仕入価格
    private String expectedDeliveryDate;    // 出荷予定日
    private String reservationDeadline;     // 予約締切日
    private MultipartFile productFile;      // 商品画像
    private MultipartFile[] detailImages;   // 詳細画像
    private String productnote;             // 商品説明
    private int purchaseLimit;              // 購入数量
    private double weight;                  // 商品重量
    private List<String> existingDetailImages;
    private List<String> deletedDetailImages;
}
