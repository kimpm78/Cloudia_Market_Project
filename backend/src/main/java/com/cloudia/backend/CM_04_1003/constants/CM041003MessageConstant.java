package com.cloudia.backend.CM_04_1003.constants;

public final class CM041003MessageConstant {

    private CM041003MessageConstant() {
    }

    public static final String QNA_LIST_SUCCESS = "Q&A 목록을 조회했습니다.";
    public static final String QNA_LIST_FAIL = "Q&A 목록을 조회하지 못했습니다.";
    public static final String QNA_DETAIL_SUCCESS = "Q&A 상세를 조회했습니다.";
    public static final String QNA_DETAIL_FAIL = "Q&A 상세를 조회하지 못했습니다.";
    public static final String QNA_CREATE_SUCCESS = "Q&A를 등록했습니다.";
    public static final String QNA_CREATE_FAIL = "Q&A 등록에 실패했습니다.";
    public static final String QNA_ANSWER_SUCCESS = "Q&A 답변을 등록했습니다.";
    public static final String QNA_ANSWER_FAIL = "Q&A 답변 등록에 실패했습니다.";
    public static final String QNA_ANSWER_FORBIDDEN = "관리자만 답변을 등록할 수 있습니다.";
    public static final String QNA_DELETE_SUCCESS = "Q&A를 삭제했습니다.";
    public static final String QNA_DELETE_FAIL = "Q&A 삭제에 실패했습니다.";
    public static final String QNA_PRIVATE_FORBIDDEN = "비공개 Q&A는 작성자와 관리자만 조회할 수 있습니다.";
    public static final String QNA_DELETE_FORBIDDEN = "작성자 또는 관리자만 삭제할 수 있습니다.";
    public static final String QNA_NOT_FOUND = "요청한 Q&A를 찾을 수 없습니다.";
    public static final String USER_NOT_FOUND = "사용자 정보를 찾을 수 없습니다.";
    public static final String INVALID_PAGING = "페이지 정보가 올바르지 않습니다.";
    public static final String INVALID_REQUEST = "요청 정보가 올바르지 않습니다.";
}
