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
    private Long attachmentId;       // 一意ID
    private Long reviewId;           // 紐づくレビューID
    private String fileName;         // 保存されたファイル名
    private String filePath;         // アクセス可能なURL
    private String fileType;         // MIMEタイプ（例: image/jpeg）
    private Long fileSize;           // ファイルサイズ（byte単位）
    private String createdBy;        // 作成者（loginId）
    private LocalDateTime createdAt; // 作成日時
}