package com.cloudia.backend.CM_02_1000.model;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class BannerInfo {
    private int bannerId;               // バナーID
    @NotBlank(message = "バナー名は必須入力です。")
    private String bannerName;          // バナー名
    @NotBlank(message = "リンクは必須入力です。")
    private String urlLink;             // バナーリンク
    private String imageLink;           // 画像リンク
    @NotNull(message = "表示可否は必須入力です。")
    private int isDisplay;              // 表示可否
    @NotNull(message = "表示順は必須入力です。")
    @Min(value = 0, message = "表示順は0以上で入力してください。")
    @Max(value = 8, message = "表示順は8以下で入力してください。")
    private int displayOrder;           // 表示順
    private String createdBy;           // 登録者
    private LocalDateTime createdAt;    // 登録日時
    private String updatedBy;           // 更新者
    private LocalDateTime updatedAt;    // 更新日時
    private MultipartFile imageFile;    // 画像ファイル

}