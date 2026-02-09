package com.cloudia.backend.CM_01_1015.model;

import lombok.Data;
import java.util.List;

@Data
public class ReturnResponse {
    private int returnId;
    private String orderNo;
    private String requestedAt;
    private String completedAt;
    private int returnStatusValue;
    private String returnStatusName;
    private String reason;
    private String productName;
    private String imageUrls;
    private String trackingNumber;
    private String courier;

    private List<ProductInfo> products;

    @Data
    public static class ProductInfo {
        private String productCode;
        private String productName;
        private int quantity;
    }
}