package com.cloudia.backend.CM_01_1006.service;

import com.cloudia.backend.CM_01_1006.model.InquiryDetailResponse;
import com.cloudia.backend.CM_01_1006.model.InquiryProductDTO;
import com.cloudia.backend.CM_01_1006.model.InquiryResponseDTO;
import com.cloudia.backend.CM_01_1006.model.InquiryWriteDTO;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CM011006Service {

    /**
     * 自分の問い合わせ履歴を取得
     */
    List<InquiryResponseDTO> getMyInquiries(String loginId);

    /**
     * 問い合わせ作成用の商品一覧を取得
     */
    List<InquiryProductDTO> getProductList();

    /**
     * 1:1問い合わせ登録
     */
    InquiryResponseDTO createInquiry(String loginId, InquiryWriteDTO writeDTO);

    /**
     * 1:1問い合わせ詳細取得
     */
    ResponseEntity<InquiryDetailResponse> getInquiryDetail(Long inquiryId, String loginId);

    /**
     * 1:1問い合わせ削除
     */
    ResponseEntity<Void> deleteInquiry(Long inquiryId, String loginId);
}