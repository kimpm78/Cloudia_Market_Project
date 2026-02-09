package com.cloudia.backend.CM_01_1006.service;

import com.cloudia.backend.CM_01_1006.model.InquiryDetailResponse;
import com.cloudia.backend.CM_01_1006.model.InquiryProductDTO;
import com.cloudia.backend.CM_01_1006.model.InquiryResponseDTO;
import com.cloudia.backend.CM_01_1006.model.InquiryWriteDTO;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CM011006Service {

    /**
     * 내 문의 내역 조회
     */
    List<InquiryResponseDTO> getMyInquiries(String loginId); // [수정]

    /**
     * 문의 작성용 상품 목록 조회
     */
    List<InquiryProductDTO> getProductList();

    /**
     * 1:1 문의 등록
     */
    InquiryResponseDTO createInquiry(String loginId, InquiryWriteDTO writeDTO); // [수정]

    /**
     * 1:1 문의 상세 조회
     */
    ResponseEntity<InquiryDetailResponse> getInquiryDetail(Long inquiryId, String loginId); // [수정]

    /**
     * 1:1 문의 삭제
     */
    ResponseEntity<Void> deleteInquiry(Long inquiryId, String loginId); // [수정]
}