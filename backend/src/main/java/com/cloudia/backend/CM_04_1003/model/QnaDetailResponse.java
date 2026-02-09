package com.cloudia.backend.CM_04_1003.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class QnaDetailResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private QnaDetail current;
    private QnaSummary prev;
    private QnaSummary next;
}
