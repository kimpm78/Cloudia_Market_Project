package com.cloudia.backend.CM_04_1003.service;

import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_04_1003.model.QnaAnswerRequest;
import com.cloudia.backend.CM_04_1003.model.QnaCreateRequest;
import com.cloudia.backend.CM_04_1003.model.QnaCreateResponse;
import com.cloudia.backend.CM_04_1003.model.QnaDetailResponse;
import com.cloudia.backend.CM_04_1003.model.QnaListResponse;
import com.cloudia.backend.CM_04_1003.model.QnaSummary;
import com.cloudia.backend.CM_04_1003.model.ResponseModel;

public interface CM041003Service {

    ResponseEntity<ResponseModel<QnaListResponse>> getQnaList(int page, int size, String searchKeyword,
            Integer searchType);

    ResponseEntity<ResponseModel<QnaDetailResponse>> getQnaDetail(Long qnaId, Long requesterId, boolean admin);

    ResponseEntity<ResponseModel<QnaCreateResponse>> createQna(QnaCreateRequest request);

    ResponseEntity<ResponseModel<Void>> answerQna(Long qnaId, QnaAnswerRequest request,
            Long answererId, String answererLoginId);

    ResponseEntity<ResponseModel<java.util.List<QnaSummary>>> getRecentQna(Integer size, String productId);

    ResponseEntity<ResponseModel<Void>> deleteQna(Long qnaId, Long requesterId, boolean admin);
}
