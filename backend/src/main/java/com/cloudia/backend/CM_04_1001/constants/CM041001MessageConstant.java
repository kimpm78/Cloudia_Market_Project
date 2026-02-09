package com.cloudia.backend.CM_04_1001.constants;

public class CM041001MessageConstant {
    private CM041001MessageConstant() {
    }
    // ========================================
    // 리뷰/후기 댓글 관련
    // ========================================

    // 등록
    public static final String COMMENT_CREATE_SUCCESS = "댓글이 성공적으로 등록되었습니다.";
    public static final String COMMENT_CREATE_FAIL = "댓글 등록 중 오류가 발생했습니다.";

    // 수정
    public static final String COMMENT_UPDATE_SUCCESS = "댓글이 성공적으로 수정되었습니다.";
    public static final String COMMENT_UPDATE_FAIL = "댓글 수정 중 오류가 발생했습니다.";

    // 삭제
    public static final String COMMENT_DELETE_SUCCESS = "댓글이 성공적으로 삭제되었습니다.";
    public static final String COMMENT_DELETE_FAIL = "댓글 삭제 중 오류가 발생했습니다.";

    // 조회
    public static final String COMMENT_FETCH_SUCCESS = "댓글 목록 조회 성공";
    public static final String COMMENT_FETCH_EMPTY = "등록된 댓글이 없습니다.";

    // 대댓글 / 트리 구조
    public static final String COMMENT_TREE_FETCH_SUCCESS = "댓글 트리 조회 성공";
    public static final String COMMENT_TREE_EMPTY = "등록된 댓글/대댓글이 없습니다.";
    public static final String REPLY_CREATE_SUCCESS = "대댓글이 성공적으로 등록되었습니다.";
    public static final String REPLY_CREATE_FAIL = "대댓글 등록 중 오류가 발생했습니다.";

    // 부모 댓글 유효성
    public static final String COMMENT_PARENT_NOT_FOUND = "부모 댓글을 찾을 수 없습니다.";
    public static final String COMMENT_SELF_REPLY_FORBIDDEN = "본인 댓글에는 답글을 작성할 수 없습니다.";

    // 댓글 유효성/예외
    public static final String COMMENT_VALIDATION_FAIL = "댓글 유효성 검사 실패: {}";
    public static final String COMMENT_DB_ERROR = "댓글 처리 중 DB 오류 발생: {}";
    public static final String COMMENT_UNEXPECTED_ERROR = "댓글 처리 중 예상치 못한 오류 발생: {}";

    // 인증 (권한)
    public static final String AUTH_REQUIRED = "로그인이 필요합니다.";
    public static final String AUTH_FORBIDDEN = "권한이 없습니다.";
}