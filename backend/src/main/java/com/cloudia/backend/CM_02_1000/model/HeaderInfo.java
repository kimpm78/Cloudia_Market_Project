package com.cloudia.backend.CM_02_1000.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * ヘッダー領域で使用される情報を保持するDTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HeaderInfo {
    private List<HeaderMenu> menus; // ヘッダーメニュー一覧
    private Cart cart;              // カート要約情報
}
