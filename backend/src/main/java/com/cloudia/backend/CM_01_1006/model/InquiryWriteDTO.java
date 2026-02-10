package com.cloudia.backend.CM_01_1006.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryWriteDTO {
    @NotBlank(message = "タイトルを入力してください。")
    private String title;        // タイトル

    @NotBlank(message = "内容を入力してください。")
    private String content;      // すべてのメタ情報を含む本文

    @NotNull(message = "公開/非公開を選択してください。")
    private Integer isPrivate;    // 公開区分（0: 公開, 1: 非公開）

    @NotBlank(message = "問い合わせ種別を選択してください。")
    private String categoryCode;  // 共通コードグループ「012」の code_value（例: "1", "2"）

}