package com.cloudia.backend.CM_01_1006.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryDetailResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private InquiryResponseDTO current;
    private InquiryResponseDTO prev;
    private InquiryResponseDTO next;
}