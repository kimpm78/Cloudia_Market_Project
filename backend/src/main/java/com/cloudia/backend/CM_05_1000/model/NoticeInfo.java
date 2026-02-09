package com.cloudia.backend.CM_05_1000.model;

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
    private int noticeId;           // お知らせID
    @NotBlank(message = "タイトルは必須入力項目です。")
    private String title;            // タイトル
    private String content;          // 内容
    private int codeValue;           // 分類
    private int isDisplay;           // 表示可否
    private int pinned;              // 上部固定フラグ
    private int viewCount;           // 閲覧数
    private Date publishedAt;        // 公開日
    private String userId;           // 投稿者
    private Integer roleId;          // 権限ID
    private String createdBy;        // 登録者
    private LocalDateTime createdAt; // 登録日時
    private String updatedBy;        // 更新者
    private LocalDateTime updatedAt; // 更新日時
}
