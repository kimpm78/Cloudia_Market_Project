package com.cloudia.backend.CM_90_1044.model;

import java.sql.Date;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
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
public class NoticeInfo {
    private int noticeId;            // お知らせID
    @NotBlank(message = "タイトル名は必須入力項目です。")
    private String title;            // タイトル名
    private String content;          // 内容
    private int codeValue;           // 分類
    private int isDisplay;           // 表示有無
    private int pinned;              // 上部固定有無
    private int viewCount;           // 閲覧数
    private Date publishedAt;        // 公開日
    private String userId;           // 登録者ID
    private String createdBy;        // 登録者
    private LocalDateTime createdAt; // 登録日
    private String updatedBy;        // 変更者
    private LocalDateTime updatedAt; // 変更日
}
