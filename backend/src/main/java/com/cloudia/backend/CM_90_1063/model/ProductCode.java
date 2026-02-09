package com.cloudia.backend.CM_90_1063.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class ProductCode {
    @NotBlank(message = "상품 코드는 필수입니다.")
    private String productCode; // 상품 코드

    @NotBlank(message = "상품명은 필수입니다.")
    private String productName; // 상품명

    @NotBlank(message = "가격은 필수입니다.")
    @Pattern(regexp = "^\\d+$", message = "가격에 소수점은 안됩니다.")
    private String productPrice; // 가격

    @NotBlank(message = "수량은 필수입니다.")
    @Pattern(regexp = "^-?\\d+$", message = "수량은 정수여야 합니다. (음수 가능)")
    private String quantity; // 수량

    private String note; // 비고
}
