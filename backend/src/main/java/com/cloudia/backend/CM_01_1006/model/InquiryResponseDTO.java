package com.cloudia.backend.CM_01_1006.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long inquiryId;                 // 問い合わせID
    private String memberNumber;            // 会員番号
    private String loginId;                 // 作成者ログインID
    private String writerName;              // 作成者名
    private String title;                   // タイトル
    private String content;                 // 本文（一覧取得時は null の場合あり）
    private Integer statusValue;            // ステータス値（1: 回答待ち, 2: 回答済み）
    private String statusCode;              // ステータスコード（例: "PENDING", "ANSWERED"）
    private String statusLabel;             // ステータス表示名（例: "回答待ち", "回答済み"）
    private String categoryName;            // カテゴリ名（例: "配送に関する問い合わせ", "その他" など）
    private Integer isPrivate;              // 公開区分（0: 公開, 1: 非公開）
    private LocalDateTime createdAt;        // 作成日時
    private LocalDateTime updatedAt;        // 更新日時

    private boolean isAnswered;             // 回答有無
    private String answerContent;           // 回答内容
    private Long answererId;                // 回答者ユーザーID
    private Integer answererRoleId;         // 回答者ロールID
    private String answererLoginId;         // 回答者ログインID
    private String answererName;            // 回答者名
    private LocalDateTime answerCreatedAt;  // 回答作成日時
}