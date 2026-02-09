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
    private boolean result;// 처리 결과
    private String message; // 메시지
    private T resultList; // 결과
}
