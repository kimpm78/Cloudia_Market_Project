package com.cloudia.backend.CM_90_1060.model;

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
    private Long boardId;            // 掲示板ID
    private String fileName;         // ファイル名
    private String filePath;         // ファイルパス
    private String contentType;      // ファイル形式
    private int codeValue;           // コード値
    private String createdBy;        // 登録者
    private LocalDateTime createdAt; // 登録日
    private String updatedBy;        // 更新者
    private LocalDateTime updatedAt; // 更新日
}
