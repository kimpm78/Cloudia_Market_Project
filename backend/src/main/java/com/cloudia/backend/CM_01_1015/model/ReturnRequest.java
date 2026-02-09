package com.cloudia.backend.CM_01_1015.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.springframework.web.multipart.MultipartFile;

import com.google.auto.value.AutoValue.Builder;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequest {
    private Integer returnId;
    private String orderNo; // 주문 번호
    private String productCode; // 신청 상품 코드
    private String memberNumber; // 회원 번호
    private int type; // 0: 환불, 1: 교환
    private String title; // 게시글 제목
    private String content; // 게시글 내용
    private MultipartFile[] files;
}