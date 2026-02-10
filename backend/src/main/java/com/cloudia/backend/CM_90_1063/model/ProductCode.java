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
    @NotBlank(message = "商品コードは必須です。")
    private String productCode;     // 商品コード

    @NotBlank(message = "商品名は必須です。")
    private String productName;     // 商品名

    @NotBlank(message = "価格は必須です。")
    @Pattern(regexp = "^\\d+$", message = "価格に小数点は使用できません。")
    private String productPrice;    // 価格

    @NotBlank(message = "数量は必須です。")
    @Pattern(regexp = "^-?\\d+$", message = "数量は整数である必要があります。（マイナス可）")
    private String quantity;        // 数量

    private String note;            // 備考
}
