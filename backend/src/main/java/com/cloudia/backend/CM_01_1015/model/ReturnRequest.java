package com.cloudia.backend.CM_01_1015.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.springframework.web.multipart.MultipartFile;

import com.google.auto.value.AutoValue.Builder;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequest {
    private Integer returnId;       // 返金・交換申請番号
    private String orderNo;         // 注文番号
    private String productCode;     // 申請商品コード
    private String memberNumber;    // 会員番号
    private int type;               // 0: 返金、1: 交換
    private String title;           // 投稿タイトル
    private String content;         // 投稿内容
    private MultipartFile[] files;  // 添付ファイル
}