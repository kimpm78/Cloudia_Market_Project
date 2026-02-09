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

    @NotBlank(message = "memberNumber는 필수입니다.")
    @Size(max = 8, message = "memberNumber는 8자 이하여야 합니다.")
    private String memberNumber;

    private Long userId;

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    @NotNull(message = "공개 여부는 필수입니다.")
    @Min(value = 0, message = "공개 여부는 0 또는 1이어야 합니다.")
    private Integer isPrivate;

    @Size(max = 10, message = "작성자는 10자 이하여야 합니다.")
    private String createdBy;

    @Size(max = 10, message = "수정자는 10자 이하여야 합니다.")
    private String updatedBy;

    private String inquiriesCodeType;

    private Integer inquiriesCodeValue;

    private Long orderId;

    @Size(max = 50, message = "주문번호는 50자 이하로 입력해주세요.")
    private String orderNumber;

    @Size(max = 10, message = "상품ID는 10자 이하로 입력해주세요.")
    private String productId;

    @Size(max = 150, message = "상품명은 150자 이하로 입력해주세요.")
    private String productName;

    @JsonProperty(access = Access.READ_ONLY)
    private Long qnaId;
}
