package com.cloudia.backend.CM_90_1044.model;

import java.sql.Date;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
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
public class NoticeInfo {
    private int noticeId; // 공지사항 아이디
    @NotBlank(message = "타이틀 명은 필수 입력값입니다.")
    private String title; // 타이틀 명
    private String content; // 내용
    private int codeValue; // 분류
    private int isDisplay; // 표시 여부
    private int pinned; // 상단 고정 여부
    private int viewCount; // 조회수
    private Date publishedAt; // 게시일
    private String userId; // 게시자
    private String createdBy; // 등록자
    private LocalDateTime createdAt; // 등록일
    private String updatedBy; // 수정자
    private LocalDateTime updatedAt; // 수정일
}
