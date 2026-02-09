package com.cloudia.backend.CM_01_1005.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {
    private String orderNo;
    private LocalDateTime orderDate;
    private String productName;
    private int orderItemCount;
    private String deliveryDate;
    private String totalPrice;
    private String orderStatus;
    private Long orderId;
    private String memberNumber;
    private Long totalAmount;
    private String paymentType;
    private int paymentValue;
    private String orderStatusType;
    private int orderStatusValue;
    private String recipientName;
    private String recipientPhone;
    private String shippingCompany;
    private String trackingNumber;
    private Long shippingCost;
    private String shippingAddress;
    private LocalDateTime paymentAt;
    private String productImageUrl;
}