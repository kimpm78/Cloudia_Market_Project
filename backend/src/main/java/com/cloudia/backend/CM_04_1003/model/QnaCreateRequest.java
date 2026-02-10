package com.cloudia.backend.CM_04_1003.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QnaCreateRequest {

    @NotBlank(message = "memberNumberは必須です。")
    @Size(max = 8, message = "memberNumberは8文字以内で入力してください。")
    private String memberNumber;

    private Long userId;

    @NotBlank(message = "タイトルを入力してください。")
    @Size(max = 255, message = "タイトルは255文字以内で入力してください。")
    private String title;

    @NotBlank(message = "内容を入力してください。")
    private String content;

    @NotNull(message = "公開/非公開は必須です。")
    @Min(value = 0, message = "公開/非公開は0または1である必要があります。")
    private Integer isPrivate;

    @Size(max = 10, message = "作成者は10文字以内で入力してください。")
    private String createdBy;

    @Size(max = 10, message = "更新者は10文字以内で入力してください。")
    private String updatedBy;

    private String inquiriesCodeType;

    private Integer inquiriesCodeValue;

    private Long orderId;

    @Size(max = 50, message = "注文番号は50文字以内で入力してください。")
    private String orderNumber;

    @Size(max = 10, message = "商品IDは10文字以内で入力してください。")
    private String productId;

    @Size(max = 150, message = "商品名は150文字以内で入力してください。")
    private String productName;

    @JsonProperty(access = Access.READ_ONLY)
    private Long qnaId;
}
