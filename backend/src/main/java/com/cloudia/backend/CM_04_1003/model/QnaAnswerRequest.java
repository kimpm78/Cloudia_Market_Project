package com.cloudia.backend.CM_04_1003.model;

import jakarta.validation.constraints.NotBlank;
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
public class QnaAnswerRequest {
    @NotBlank(message = "回答内容を入力してください。")
    private String answerContent;
}