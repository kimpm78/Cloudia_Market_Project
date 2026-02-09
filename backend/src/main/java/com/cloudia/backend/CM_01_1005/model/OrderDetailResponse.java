package com.cloudia.backend.CM_01_1005.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResponse {

    private Summary orderSummary;
    private List<Payment> paymentDetails;
    private Shipping shippingInfo;
    private UserRefundInfo userRefundInfo;

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private String orderNo;
        private String orderDate;
        private String orderStatus;
        private int orderStatusValue;
        private int paymentValue;
        private long shippingCost;
        private String productName;
        private LocalDateTime paymentAt; // 72시간 계산의 기준이 될 시각
        private UserRefundInfo userRefundInfo;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payment {
        private String product;
        private String productCode;
        private String tax;
        private String shipping;
        private int quantity;
        private String total;
        private String imageUrl;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Shipping {
        private String address;
        private String receiver;
        private String phone;
        private String tracking;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserRefundInfo {
        private String bankName;
        private String accountNumber;
        private String accountHolder;
    }
}