package com.cloudia.backend.CM_04_1003.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class QnaSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long qnaId;
    private Long userId;
    private String memberNumber;
    private String loginId;
    private String writerName;
    private String title;
    private String content;
    private Integer statusValue;
    private String statusCode;
    private String statusLabel;
    private Integer isPrivate;
    private String createdAt;
    private String updatedAt;
    private Integer viewCount;

    private String inquiriesCodeType;
    private Integer inquiriesCodeValue;
    private Long orderId;
    private String orderNumber;
    private String productId;
    private String productName;

    private String answerContent;
    private String answerCreatedAt;
}
