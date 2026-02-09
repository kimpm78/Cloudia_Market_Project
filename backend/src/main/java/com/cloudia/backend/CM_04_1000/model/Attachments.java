package com.cloudia.backend.CM_04_1000.model;

import java.time.LocalDateTime;

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
public class Attachments {
    private Long attachmentId;       // 고유 ID
    private Long reviewId;           // 연결된 리뷰 ID
    private String fileName;         // 저장된 파일명
    private String filePath;         // 접근 가능한 URL
    private String fileType;         // MIME 타입 (예: image/jpeg)
    private Long fileSize;           // 파일 크기 (byte 단위)
    private String createdBy;        // 작성자 (loginId)
    private LocalDateTime createdAt; // 작성 시각
}