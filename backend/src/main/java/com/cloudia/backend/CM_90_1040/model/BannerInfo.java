package com.cloudia.backend.CM_90_1040.model;

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
    private int bannerId; // 배너 아이디
    @NotBlank(message = "배너명은 필수 입력값입니다.")
    private String bannerName; // 배너 명
    @NotBlank(message = "링크는 필수 입력값입니다.")
    private String urlLink; // 배너 링크
    private String imageLink; // 이미지 링크
    @NotNull(message = "표시 여부는 필수 입력값입니다.")
    private int isDisplay; // 표시 여부
    @NotNull(message = "배너 순서는 필수 입력값입니다.")
    @Min(value = 0, message = "배너 순서는 0 이상이어야 합니다.")
    @Max(value = 8, message = "배너 순서는 8 이하이어야 합니다.")
    private int displayOrder; // 표시 순서
    private String createdBy; // 등록자
    private LocalDateTime createdAt; // 등록일
    private String updatedBy; // 수정자
    private LocalDateTime updatedAt; // 수정일
    private MultipartFile imageFile; // 이미지 파일
}