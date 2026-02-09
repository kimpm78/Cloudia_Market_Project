package com.cloudia.backend.CM_01_1008.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddress {
    private Integer addressId;
    private String memberNumber;
    private String addressNickname;
    private String recipientName;
    private String postalCode;
    private String addressMain;
    private String addressDetail1;
    private String addressDetail2;
    private String addressDetail3;
    private String recipientPhone;
    private Boolean isDefault;
    private Short isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
