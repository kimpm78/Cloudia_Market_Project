package com.cloudia.backend.CM_90_1040.constants;

public class CM901040MessageConstant {
    private CM901040MessageConstant() {
    }

    // 배너 삭제 관련
    public static final String BANNER_DELETE_START = "배너 삭제 시작, 삭제 대상 개수: {}";
    public static final String BANNER_DELETE_COMPLETE = "배너 삭제 완료, ID: {}, 이미지 경로: {}";
    public static final String BANNER_DELETE_SUCCESS = "배너 삭제 완료, 삭제된 개수: {}";
    public static final String BANNER_DELETE_FAILED_EMPTY_LIST = "배너 삭제 실패: 삭제할 배너 목록이 비어있습니다.";
    public static final String BANNER_DELETE_FAILED_INVALID_INFO = "유효하지 않은 배너 정보를 건너뜁니다: {}";
    public static final String BANNER_DELETE_FAILED_INTEGRITY_VIOLATION = "배너 삭제 실패 - 참조 제약조건 위반, ID: {}";
    public static final String BANNER_DELETE_FAILED_NO_RESULT = "배너 삭제 결과: 삭제된 배너가 없습니다.";
    public static final String BANNER_DELETE_DB_ERROR = "배너 삭제 중 DB 오류 발생: {}";
    public static final String BANNER_DELETE_UNEXPECTED_ERROR = "배너 삭제 중 예상치 못한 오류 발생: {}";

    // 배너 조회 관련
    public static final String BANNER_FIND_ALL_START = "배너 전체 리스트 조회 시작";
    public static final String BANNER_FIND_ALL_COMPLETE = "배너 전체 리스트 조회 완료, 조회된 배너 수: {}";
    public static final String BANNER_FIND_ALL_DB_ERROR = "배너 전체 리스트 조회 중 DB 오류 발생: {}";
    public static final String BANNER_FIND_ALL_UNEXPECTED_ERROR = "배너 전체 리스트 조회 중 예상치 못한 오류 발생: {}";

    public static final String BANNER_SEARCH_START = "배너 검색 시작, 검색어: {}";
    public static final String BANNER_SEARCH_COMPLETE = "배너 검색 완료, 조회된 배너 수: {}";
    public static final String BANNER_SEARCH_FAILED_EMPTY_TERM = "배너 검색 실패: 검색어가 비어있습니다.";
    public static final String BANNER_SEARCH_DB_ERROR = "배너 검색 중 DB 오류 발생, 검색어: {}, 오류: {}";
    public static final String BANNER_SEARCH_UNEXPECTED_ERROR = "배너 검색 중 예상치 못한 오류 발생, 검색어: {}, 오류: {}";

    public static final String BANNER_FIND_BY_ID_START = "배너 상세 조회 시작, 배너 ID: {}";
    public static final String BANNER_FIND_BY_ID_COMPLETE = "배너 상세 조회 완료, 배너 ID: {}";
    public static final String BANNER_FIND_BY_ID_FAILED_INVALID_ID = "배너 상세 조회 실패: 유효하지 않은 배너 ID입니다. 배너 ID: {}";
    public static final String BANNER_FIND_BY_ID_DB_ERROR = "배너 상세 조회 중 DB 오류 발생, 배너 ID: {}, 오류: {}";
    public static final String BANNER_FIND_BY_ID_UNEXPECTED_ERROR = "배너 상세 조회 중 예상치 못한 오류 발생, 배너 ID: {}, 오류: {}";

    // 배너 등록 / 업데이트 관련
    public static final String BANNER_UPLOAD_START = "배너 등록 시작, 배너명: {}";
    public static final String BANNER_UPLOAD_COMPLETE = "배너 등록 완료, 배너명: {}, 등록 결과: {}";
    public static final String BANNER_UPLOAD_ACTIVE_COUNT_CHECK = "현재 활성 배너 수: {}";
    public static final String BANNER_UPLOAD_FAILED_MAX_EXCEEDED = "배너 등록 실패: 활성 배너 수가 최대치({})를 초과했습니다. 현재 수: {}";
    public static final String BANNER_UPLOAD_FAILED_DUPLICATE_ORDER = "배너 등록 실패: 배너 순서가 중복되었습니다. 순서: {}";
    public static final String BANNER_FILE_SAVED = "배너 이미지 파일 저장 완료: {}";
    public static final String BANNER_UPLOAD_SECURITY_ERROR = "배너 등록 중 보안 오류 발생: {}";
    public static final String BANNER_UPLOAD_FILE_ERROR = "배너 등록 중 파일 저장 오류 발생: {}";
    public static final String BANNER_UPLOAD_DUPLICATE_KEY_ERROR = "배너 등록 중 중복 키 오류 발생: {}";
    public static final String BANNER_UPLOAD_DB_ERROR = "배너 등록 중 DB 오류 발생: {}";
    public static final String BANNER_UPLOAD_UNEXPECTED_ERROR = "배너 등록 중 예상치 못한 오류 발생: {}";
    public static final String BANNER_UPDATE_START = "배너 업데이트 시작, 배너 ID: {}, 배너명: {}";
    public static final String BANNER_UPDATE_COMPLETE = "배너 업데이트 완료, 배너 ID: {}, 업데이트 결과: {}";
    public static final String BANNER_UPDATE_FAILED_NOT_EXISTS = "배너 업데이트 실패: 존재하지 않는 배너 ID입니다. 배너 ID: {}";
    public static final String BANNER_UPDATE_FAILED_MAX_EXCEEDED = "배너 업데이트 실패: 활성 배너 수가 최대치({})를 초과합니다. 현재 수: {}";
    public static final String BANNER_UPDATE_FAILED_DUPLICATE_ORDER = "배너 업데이트 실패: 배너 순서가 중복되었습니다. 순서: {}";
    public static final String BANNER_UPDATE_SECURITY_ERROR = "배너 업데이트 중 보안 오류 발생: {}";
    public static final String BANNER_UPDATE_FILE_ERROR = "배너 업데이트 중 파일 저장 오류 발생: {}";
    public static final String BANNER_UPDATE_DUPLICATE_KEY_ERROR = "배너 업데이트 중 중복 키 오류 발생: {}";
    public static final String BANNER_UPDATE_DB_ERROR = "배너 업데이트 중 DB 오류 발생: {}";
    public static final String BANNER_UPDATE_UNEXPECTED_ERROR = "배너 업데이트 중 예상치 못한 오류 발생: {}";

    // 디스플레이 순서 관련
    public static final String DISPLAY_ORDER_FIND_START = "사용 가능한 디스플레이 번호 조회 시작";
    public static final String DISPLAY_ORDER_FIND_COMPLETE = "사용 가능한 디스플레이 번호 조회 완료, 조회된 번호 수: {}";
    public static final String DISPLAY_ORDER_FIND_DB_ERROR = "디스플레이 번호 조회 중 DB 오류 발생: {}";
    public static final String DISPLAY_ORDER_FIND_UNEXPECTED_ERROR = "디스플레이 번호 조회 중 예상치 못한 오류 발생: {}";
    public static final String DISPLAY_ORDER_DUPLICATE_CHECK_ERROR = "디스플레이 순서 중복 확인 중 오류 발생: {}";

    // 파일 관련
    public static final String FILE_UPLOAD_PATH_DEBUG = "파일 업로드 경로: {}";
    public static final String FILE_UPLOAD_DIR_CREATED = "업로드 디렉토리 생성: {}";
    public static final String FILE_UPLOAD_DIR_CREATE_FAILED = "업로드 디렉토리 생성 실패: {}";
    public static final String FILE_SAVE_COMPLETE = "파일 저장 완료: {}";
    public static final String FILE_SAVE_FAILED = "파일 저장 실패: {}";
    public static final String FILE_DELETE_FAILED_CLEANUP = "실패한 파일 삭제 중 오류 발생: {}";

    public static final String IMAGE_DELETE_NO_LINK = "삭제할 이미지 링크가 없습니다.";
    public static final String IMAGE_DELETE_COMPLETE = "이미지 파일 삭제 완료: {}";
    public static final String IMAGE_DELETE_NOT_EXISTS = "삭제할 이미지 파일이 존재하지 않습니다: {}";
    public static final String IMAGE_DELETE_FILE_NOT_FOUND = "삭제할 파일이 존재하지 않습니다: {}";
    public static final String IMAGE_DELETE_FAILED = "이미지 파일 삭제 실패: {}, 오류: {}";
    public static final String IMAGE_DELETE_UNEXPECTED_ERROR = "이미지 파일 삭제 중 예상치 못한 오류 발생: {}, 오류: {}";

    // 기타
    public static final String ACTIVE_BANNER_COUNT_ERROR = "활성 배너 수 조회 중 오류 발생: {}";

    // ========================================
    // 응답 메시지
    // ========================================

    // 성공 메시지
    public static final String SUCCESS_BANNER_DELETE = "배너 삭제 성공";
    public static final String SUCCESS_BANNER_FIND = "배너 조회 성공";
    public static final String SUCCESS_BANNER_UPLOAD = "배너 등록 성공";
    public static final String SUCCESS_BANNER_UPDATE = "배너 업데이트 성공";
    public static final String SUCCESS_DISPLAY_ORDER_FIND = "디스플레이 번호 조회 성공";

    // 실패 메시지
    public static final String FAIL_NO_BANNER_SELECTED = "삭제할 배너가 선택되지 않았습니다.";
    public static final String FAIL_REFERENCED_BANNER = "다른 데이터에서 참조 중인 배너는 삭제할 수 없습니다.";
    public static final String FAIL_BANNER_NOT_FOUND = "삭제할 배너를 찾을 수 없습니다.";
    public static final String FAIL_BANNER_NOT_EXISTS = "존재하지 않는 배너입니다.";
    public static final String FAIL_SEARCH_TERM_REQUIRED = "검색어를 입력해주세요.";
    public static final String FAIL_INVALID_BANNER_ID = "유효하지 않은 배너 ID입니다.";
    public static final String FAIL_MAX_ACTIVE_BANNERS = "활성화된 배너의 수량이 최대 8개를 초과했습니다.";
    public static final String FAIL_DUPLICATE_DISPLAY_ORDER = "배너 순서가 중복되었습니다.";
    public static final String FAIL_INVALID_FILE_TYPE = "허용되지 않는 파일 형식입니다.";
    public static final String FAIL_DUPLICATE_BANNER_INFO = "이미 존재하는 배너 정보입니다.";
    public static final String FAIL_DUPLICATE_BANNER_UPDATE = "중복된 배너 정보입니다.";
    public static final String FAIL_BANNER_VAL = "배너 등록 검증 실패: {}";
    public static final String FAIL_BANNER_UPDATE = "해당 배너는 다른 사용자가 수정하였습니다. 다시 조회 후 수정해주세요.";

}
