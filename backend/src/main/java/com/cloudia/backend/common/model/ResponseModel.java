package com.cloudia.backend.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ResponseModel<T> {
    private boolean result;   // 処理結果
    private String message;   // メッセージ
    private T resultList;     // 結果
}
