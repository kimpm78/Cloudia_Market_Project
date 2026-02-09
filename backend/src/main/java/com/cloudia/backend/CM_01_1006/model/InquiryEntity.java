package com.cloudia.backend.CM_01_1006.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryEntity {

    private Long inquiryId;
    private String memberNumber;
    private String title;
    private String content;
    private Integer isPrivate;
    private String inquiryStatusType;
    private Integer inquiryStatusValue;
    private String inquiriesCodeType;
    private Integer inquiriesCodeValue;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String answerContent;
    private LocalDateTime answeredAt;
    private String answeredBy;
}