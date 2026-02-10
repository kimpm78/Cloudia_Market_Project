package com.cloudia.backend.CM_04_1003.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QnaCreateResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long qnaId;
}
