package com.cloudia.backend.CM_04_1003.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class QnaListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<QnaSummary> items = Collections.emptyList();
    private long totalCount;
    private int page;
    private int size;
}
