package com.cloudia.backend.CM_04_1003.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class QnaDetail extends QnaSummary {
    private static final long serialVersionUID = 1L;

    private Long answererId;
    private String answererLoginId;
    private String answererName;
}
