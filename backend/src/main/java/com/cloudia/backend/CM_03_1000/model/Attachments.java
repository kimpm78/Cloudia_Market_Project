package com.cloudia.backend.CM_03_1000.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * エディタ画像情報を表現するためのDTO
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Attachments {
    private Long attachmentId;        // 添付ファイルID
    private String productCode;       // 商品コード
    private String fileName;          // ファイル名
    private String filePath;          // ファイルパス
    private String fileType;          // ファイル種別
    private Long fileSize;            // ファイルサイズ
    private String createdBy;         // 作成者
    private LocalDateTime createdAt;  // 作成日時
}
