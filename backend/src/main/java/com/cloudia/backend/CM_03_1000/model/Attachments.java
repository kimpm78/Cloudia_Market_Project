package com.cloudia.backend.CM_03_1000.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 에디터 이미지 정보를 표현하기 위한 DTO.
 * 마이바티스 매퍼에서 조회 결과를 매핑할 때 사용한다.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Attachments {
    private Long attachmentId;
    private String productCode;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String createdBy;
    private LocalDateTime createdAt;
}
