package com.cloudia.backend.CM_05_1000.constants;

public class CM051000MessageConstant {
    private CM051000MessageConstant() {
    }

    // 공지사항 조회 관련
    public static final String NOTICE_FIND_ALL_START = "공지사항 전체 리스트 조회 시작";
    public static final String NOTICE_FIND_ALL_COMPLETE = "공지사항 전체 리스트 조회 완료, 조회된 공지사항 수: {}";
    public static final String NOTICE_FIND_ALL_DB_ERROR = "공지사항 전체 리스트 조회 중 DB 오류 발생: {}";
    public static final String NOTICE_FIND_ALL_UNEXPECTED_ERROR = "공지사항 전체 리스트 조회 중 예상치 못한 오류 발생: {}";

    public static final String NOTICE_SEARCH_START = "공지사항 검색 시작, 검색어: {}, 검색 타입: {}";
    public static final String NOTICE_SEARCH_COMPLETE = "공지사항 검색 완료, 조회된 공지사항 수: {}";
    public static final String NOTICE_SEARCH_FAILED_EMPTY_TERM = "공지사항 검색 실패: 검색어가 비어있습니다.";
    public static final String NOTICE_SEARCH_FAILED_INVALID_TYPE = "공지사항 검색 실패: 유효하지 않은 검색 타입입니다. 검색 타입: {}";
    public static final String NOTICE_SEARCH_DB_ERROR = "공지사항 검색 중 DB 오류 발생, 검색어: {}, 검색 타입: {}, 오류: {}";
    public static final String NOTICE_SEARCH_UNEXPECTED_ERROR = "공지사항 검색 중 예상치 못한 오류 발생, 검색어: {}, 검색 타입: {}, 오류: {}";

    public static final String NOTICE_FIND_BY_ID_START = "공지사항 상세 조회 시작, 공지사항 ID: {}";
    public static final String NOTICE_FIND_BY_ID_COMPLETE = "공지사항 상세 조회 완료, 공지사항 ID: {}, 조회된 공지사항 수: {}";
    public static final String NOTICE_FIND_BY_ID_FAILED_INVALID_ID = "공지사항 상세 조회 실패: 유효하지 않은 공지사항 ID입니다. 공지사항 ID: {}";
    public static final String NOTICE_FIND_BY_ID_DB_ERROR = "공지사항 상세 조회 중 DB 오류 발생, 공지사항 ID: {}, 오류: {}";
    public static final String NOTICE_FIND_BY_ID_UNEXPECTED_ERROR = "공지사항 상세 조회 중 예상치 못한 오류 발생, 공지사항 ID: {}, 오류: {}";

    // 공지사항 조회수 관련
    public static final String NOTICE_VIEW_INCREMENT_SUCCESS = "공지사항 조회수 증가 성공";
    public static final String NOTICE_VIEW_INCREMENT_FAIL = "공지사항 조회수 증가 실패";
    public static final String NOTICE_VIEW_INCREMENT_ERROR = "공지사항 조회수 증가 중 오류 발생";
    public static final String NOTICE_VIEW_ALREADY_COUNTED = "오늘 이미 조회수가 반영되었습니다. key={}";

    // 공지사항 등록 / 업데이트 관련
    public static final String NOTICE_UPLOAD_START = "공지사항 등록 시작, 제목: {}";
    public static final String NOTICE_UPLOAD_COMPLETE = "공지사항 등록 완료, 제목: {}, 등록 결과: {}";
    public static final String NOTICE_UPLOAD_DUPLICATE_KEY_ERROR = "공지사항 등록 중 중복 키 오류 발생: {}";
    public static final String NOTICE_UPLOAD_DB_ERROR = "공지사항 등록 중 DB 오류 발생: {}";
    public static final String NOTICE_UPLOAD_UNEXPECTED_ERROR = "공지사항 등록 중 예상치 못한 오류 발생: {}";
    public static final String NOTICE_UPLOAD_FAILED_EMPTY_LIST = "공지사항 삭제 실패: 등록할 공지사항 목록이 비어있습니다.";

    public static final String NOTICE_UPDATE_START = "공지사항 업데이트 시작, 공지사항 ID: {}, 제목: {}";
    public static final String NOTICE_UPDATE_COMPLETE = "공지사항 업데이트 완료, 공지사항 ID: {}, 업데이트 결과: {}";
    public static final String NOTICE_UPDATE_FAILED_NOT_EXISTS = "공지사항 업데이트 실패: 존재하지 않는 공지사항 ID입니다. 공지사항 ID: {}";
    public static final String NOTICE_UPDATE_DUPLICATE_KEY_ERROR = "공지사항 업데이트 중 중복 키 오류 발생: {}";
    public static final String NOTICE_UPDATE_DB_ERROR = "공지사항 업데이트 중 DB 오류 발생: {}";
    public static final String NOTICE_UPDATE_UNEXPECTED_ERROR = "공지사항 업데이트 중 예상치 못한 오류 발생: {}";
    public static final String NOTICE_UPDATE_FAILED_EMPTY_LIST = "공지사항 삭제 실패: 업데이트할 공지사항 목록이 비어있습니다.";

    // ========================================
    // 응답 메시지
    // ========================================

    // 성공 메시지
    public static final String SUCCESS_NOTICE_FIND = "공지사항 조회 성공";
    public static final String SUCCESS_NOTICE_UPLOAD = "공지사항 등록 성공";
    public static final String SUCCESS_NOTICE_UPDATE = "공지사항 업데이트 성공";

    // 실패 메시지
    public static final String FAIL_NO_NOTICE_SELECTED = "삭제할 공지사항이 선택되지 않았습니다.";
    public static final String FAIL_NOTICE_NOT_EXISTS = "존재하지 않는 공지사항입니다.";
    public static final String FAIL_SEARCH_TERM_REQUIRED = "검색어를 입력해주세요.";
    public static final String FAIL_SEARCH_TYPE_INVALID = "유효하지 않은 검색 타입입니다.";
    public static final String FAIL_INVALID_NOTICE_ID = "유효하지 않은 공지사항 ID입니다.";
    public static final String FAIL_DUPLICATE_NOTICE_TITLE = "이미 존재하는 공지사항 제목입니다.";
    public static final String FAIL_DUPLICATE_NOTICE_INFO = "이미 존재하는 공지사항 정보입니다.";
    public static final String FAIL_DUPLICATE_NOTICE_UPDATE = "중복된 공지사항 정보입니다.";
    public static final String FAIL_NOTICE_VAL = "공지사항 등록 검증 실패: {}";

    // 공지사항 삭제 관련
    public static final String NOTICE_DELETE_START = "공지사항 삭제 시작, 공지사항 ID: {}";
    public static final String NOTICE_DELETE_COMPLETE = "공지사항 삭제 완료, 공지사항 ID: {}, 삭제 결과: {}";
    public static final String NOTICE_DELETE_FAILED_INVALID_ID = "공지사항 삭제 실패: 유효하지 않은 공지사항 ID입니다. 공지사항 ID: {}";
    public static final String NOTICE_DELETE_FAILED_NOT_EXISTS = "공지사항 삭제 실패: 존재하지 않는 공지사항입니다. 공지사항 ID: {}";
    public static final String NOTICE_DELETE_DB_ERROR = "공지사항 삭제 중 DB 오류 발생, 공지사항 ID: {}, 오류: {}";
    public static final String NOTICE_DELETE_UNEXPECTED_ERROR = "공지사항 삭제 중 예상치 못한 오류 발생, 공지사항 ID: {}, 오류: {}";

    public static final String SUCCESS_NOTICE_DELETE = "공지사항 삭제 성공"; 
}