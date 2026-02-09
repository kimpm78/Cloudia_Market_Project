package com.cloudia.backend.CM_01_1006.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryProductDTO {
    private String productCode;
    private String productName;
}