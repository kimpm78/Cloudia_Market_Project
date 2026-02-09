package com.cloudia.backend.CM_90_1060.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Attachments {
    private Long boardId; // 게시판 아이디
    private String fileName; // 파일명
    private String filePath; // 파일 경로
    private String contentType; // 파일 형식
    private int codeValue; // 코드 값
    private String createdBy; // 등록자
    private LocalDateTime createdAt; // 등록일
    private String updatedBy; // 수정자
    private LocalDateTime updatedAt; // 수정일
}
